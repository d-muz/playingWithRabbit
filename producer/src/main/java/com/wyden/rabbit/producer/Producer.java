package com.wyden.rabbit.producer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.wyden.rabbit.RabbitConfig.WORKER_OUTBOUND_QUEUE_NAME;

@Slf4j
@Component
public class Producer {

    private final FanoutExchange exchangeInbound;
    private final FanoutExchange exchangeCertified;
    private final FanoutExchange exchangeDiscarded;
    private final RabbitTemplate rabbitTemplate;

    @Getter
    @Setter
    private boolean isProducingEnabled = false;

    @Getter
    @Setter
    private int discardMessageTimeoutInSeconds = 10;

    public Producer(@Qualifier("workInboundExchange") FanoutExchange exchangeInbound,
                    @Qualifier("certifiedResultExchange") FanoutExchange exchangeCertified,
                    @Qualifier("discardedResultExchange") FanoutExchange exchangeDiscarded,
                    @Value("${producing:false}") boolean isProducingEnabled,
                    RabbitTemplate rabbitTemplate) {
        this.exchangeInbound = exchangeInbound;
        this.exchangeCertified = exchangeCertified;
        this.exchangeDiscarded = exchangeDiscarded;
        this.rabbitTemplate = rabbitTemplate;
        this.isProducingEnabled = isProducingEnabled;
    }

    @Scheduled(fixedRateString = "${delayInMillis}")
    public void produce() {
        if (!isProducingEnabled) {
            return;
        }

        String message = "" + Instant.now().getEpochSecond();
        this.rabbitTemplate.convertAndSend(exchangeInbound.getName(), "", message);
        log.info("Producer: sent message {} to exchange {}", message, exchangeInbound.getName());
    }

    @RabbitListener(queues = WORKER_OUTBOUND_QUEUE_NAME)
    public void commitMessage(String message) {
        String certifiedMessage = message + "-certified";
        this.rabbitTemplate.convertAndSend(exchangeCertified.getName(), "", certifiedMessage);
        log.info("Message certified: {}", certifiedMessage);
    }

    private void discardMessage(String message) {
        String discardedMessage = message + "-discarded";
        this.rabbitTemplate.convertAndSend(exchangeDiscarded.getName(), "", discardedMessage);
        log.info("Message discarded: {}", discardedMessage);
    }
}
