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
import java.util.Date;

import static com.wyden.rabbit.RabbitConfig.WORKER_OUTBOUND_QUEUE_NAME;

@Slf4j
@Component
public class Producer {

    private final FanoutExchange exchangeInbound;
    private final FanoutExchange exchangeCertified;
    private final RabbitTemplate rabbitTemplate;

    @Getter
    @Setter
    private boolean isProducingEnabled = false;

    public Producer(@Qualifier("workInboundExchange") FanoutExchange exchangeInbound,
                    @Qualifier("certifiedResultExchange") FanoutExchange exchangeCertified,
                    @Value("${producing:false}") boolean isProducingEnabled,
                    RabbitTemplate rabbitTemplate) {
        this.exchangeInbound = exchangeInbound;
        this.exchangeCertified = exchangeCertified;
        this.rabbitTemplate = rabbitTemplate;
        this.isProducingEnabled = isProducingEnabled;
    }

    @Scheduled(fixedRateString = "${delayInMillis}")
    public void produce() {
        if (!isProducingEnabled) {
            return;
        }

        Instant now = Instant.now();
        this.rabbitTemplate.convertAndSend(exchangeInbound.getName(), "", "" + now.getEpochSecond(), messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setTimestamp(Date.from(now));
            return messagePostProcessor;
        });
        log.info("Producer: sent message {} to exchange {}", now.getEpochSecond(), exchangeInbound.getName());
    }

    @RabbitListener(queues = WORKER_OUTBOUND_QUEUE_NAME)
    public void commitMessage(String message) {
        String certifiedMessage = message + "-certified";
        this.rabbitTemplate.convertAndSend(exchangeCertified.getName(), "", certifiedMessage);
        log.info("Message certified: {}", certifiedMessage);
    }
}
