package com.wyden.rabbit.producer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Producer {

    private final FanoutExchange exchangeInbound;
    private final FanoutExchange exchangeCertified;
    private final FanoutExchange exchangeDiscarded;
    private final MessageSender<String> messageSender;
    private final RabbitTemplate rabbitTemplate;

    @Getter
    @Setter
    private boolean isRunning = true;

    @Getter
    @Setter
    private int discardMessageTimeoutInSeconds = 10;

    public Producer(@Qualifier("workInboundExchange") FanoutExchange exchangeInbound,
                    @Qualifier("certifiedResultExchange") FanoutExchange exchangeCertified,
                    @Qualifier("discardedResultExchange") FanoutExchange exchangeDiscarded,
                    MessageSender<String> messageSender,
                    RabbitTemplate rabbitTemplate) {
        this.exchangeInbound = exchangeInbound;
        this.exchangeCertified = exchangeCertified;
        this.exchangeDiscarded = exchangeDiscarded;
        this.messageSender = messageSender;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRateString = "${delayInMillis}")
    public void produce() {
        if (!isRunning) {
            return;
        }

        String message = "" + Instant.now().getEpochSecond();
        CompletableFuture<String> future = this.messageSender.sendMessage(exchangeInbound.getName(), "", message);
        log.info("Producer: sent message {} to exchange {}", message, exchangeInbound.getName());
        future.orTimeout(discardMessageTimeoutInSeconds, TimeUnit.SECONDS)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    commitMessage(result);
                } else {
                    discardMessage(message);
                }
            });

    }

    private void commitMessage(String message) {
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
