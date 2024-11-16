package com.wyden.rabbit.worker;

import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;

import java.io.IOException;
import java.time.Instant;

import static com.wyden.rabbit.RabbitConfig.MESSAGE_TTL_IN_MILLIS;
import static com.wyden.rabbit.RabbitConfig.WORKER_INBOUND_QUEUE_NAME;

@Slf4j
@Configuration
public class Worker {

    private final FanoutExchange workOutboundExchange;
    private final RabbitTemplate rabbitTemplate;

    @Getter
    @Setter
    private boolean exceptionThrowingIsActive = false;

    @Getter
    @Setter
    private int exceptionRatio = 3;
    private long counter = 0;

    @Getter
    @Setter
    private int timeDelayInSeconds = 1;

    public Worker(@Qualifier("workOutboundExchange") FanoutExchange workOutboundExchange,
                  RabbitTemplate rabbitTemplate) {
        this.workOutboundExchange = workOutboundExchange;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = WORKER_INBOUND_QUEUE_NAME)
    public void process(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            String messageBody = new String(message.getBody());
            if (isMessageWithoutCreationDate(message) || isMessageOutdated(message)) {
                //for valid TTL configuration on queue and prefetch count = 1 it shouldn't occur
                discardMessage(channel, tag);
                log.warn("Message {} discarded - missing creation date or TTL reached before processing... ", messageBody);
                return;
            }

            String processedMessage = processMessage(messageBody);

            //reject message if ttl was reached during long processing
            if (isMessageOutdated(message)) {
                log.warn("Message {} discarded - TTL reached during processing... ", messageBody);
                discardMessage(channel, tag);
                return;
            }

            this.rabbitTemplate.convertAndSend(workOutboundExchange.getName(), "", processedMessage);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Processing exception: {}", e.getMessage(), e);
            try {
                channel.basicReject(tag, true);
            } catch (Exception ex) {
                log.error("Message reject (requeue) exception: {}", ex.getMessage(), ex);
            }
        }
    }

    private String processMessage(String message) {

        if (exceptionThrowingIsActive && exceptionRatio > 0 && ++counter % exceptionRatio == 0) {
            throw new RuntimeException("Message processing error (requeue):" + message);
        }

        try {
            Thread.sleep(timeDelayInSeconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Processed message: {}", message);
        return message + "-processed";
    }

    private void discardMessage(Channel channel, long tag) {
        try {
            channel.basicReject(tag, false);
        } catch (IOException e) {
            log.error("Message discard exception: {}", e.getMessage(), e);
        }
    }

    private boolean isMessageWithoutCreationDate(Message message) {
        return message.getMessageProperties().getTimestamp() == null;
    }

    private boolean isMessageOutdated(Message message) {
        return 1000 * (Instant.now().getEpochSecond() - message.getMessageProperties().getTimestamp().toInstant().getEpochSecond()) > MESSAGE_TTL_IN_MILLIS;
    }

}
