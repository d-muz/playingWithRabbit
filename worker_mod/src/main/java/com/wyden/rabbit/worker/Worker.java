package com.wyden.rabbit.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import static com.wyden.rabbit.worker.WorkerCfg.WORKER_INBOUND_QUEUE_NAME;

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
    public String process(String message) {
        try {
            Thread.sleep(timeDelayInSeconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (exceptionThrowingIsActive && exceptionRatio > 0 && ++counter % exceptionRatio == 0) {
            throw new RuntimeException("Reject message:" + message);
        }

        log.info("Processing: {}", message);
        String result = message + "-processed";
        this.rabbitTemplate.convertAndSend(workOutboundExchange.getName(), "", result);
        return result;
    }


}
