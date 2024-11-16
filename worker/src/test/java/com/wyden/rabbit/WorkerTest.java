package com.wyden.rabbit;

import com.wyden.rabbit.worker.Worker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.RabbitConverterFuture;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.wyden.rabbit.RabbitConfig.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
public class WorkerTest {

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("workOutboundExchange")
    FanoutExchange exchange;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private Worker worker;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitContainer::getAdminPassword);
    }


    @Test
    void messageProcess_success() throws InterruptedException, ExecutionException {

        String inboundMessage = "INBOUND_MESSAGE";
        worker.setTimeDelayInSeconds(1);
        int messageCountBeforeTest = rabbitAdmin.getQueueInfo(WORKER_OUTBOUND_QUEUE_NAME).getMessageCount();

        this.rabbitTemplate.convertAndSend(WORK_INBOUND_EXCHANGE_NAME, "", inboundMessage, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setExpiration("" + MESSAGE_TTL_IN_MILLIS);
            messagePostProcessor.getMessageProperties().setTimestamp(new Date());
            return messagePostProcessor;
        });

        Thread.sleep(worker.getTimeDelayInSeconds() * 1000L + 500L);

        Assertions.assertEquals(messageCountBeforeTest + 1, rabbitAdmin.getQueueInfo(WORKER_OUTBOUND_QUEUE_NAME).getMessageCount());

    }

    @Test
    void messageProcess_ttlReachedDuringProcessing() throws InterruptedException, ExecutionException {

        String inboundMessage = "INBOUND_MESSAGE";
        worker.setTimeDelayInSeconds(MESSAGE_TTL_IN_MILLIS / 1000 + 1);

        int messageCountBeforeTest = rabbitAdmin.getQueueInfo(AUDIT_DISCARDED_QUEUE_NAME).getMessageCount();

        this.rabbitTemplate.convertAndSend(WORK_INBOUND_EXCHANGE_NAME, "", inboundMessage, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setTimestamp(new Date());
            return messagePostProcessor;
        });

        Thread.sleep(worker.getTimeDelayInSeconds() * 1000L + 500L);

        Assertions.assertEquals(messageCountBeforeTest + 1, rabbitAdmin.getQueueInfo(AUDIT_DISCARDED_QUEUE_NAME).getMessageCount());

    }

    @Test
    void messageProcess_ttlReachedBeforeProcessing() throws InterruptedException, ExecutionException {

        String inboundMessage = "INBOUND_MESSAGE";
        worker.setTimeDelayInSeconds(MESSAGE_TTL_IN_MILLIS / 1000 + 1);

        int messageCountBeforeTest = rabbitAdmin.getQueueInfo(AUDIT_DISCARDED_QUEUE_NAME).getMessageCount();

        this.rabbitTemplate.convertAndSend(WORK_INBOUND_EXCHANGE_NAME, "", inboundMessage, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setTimestamp(Date.from(Instant.now().minusMillis(MESSAGE_TTL_IN_MILLIS * 2L)));
            return messagePostProcessor;
        });

        Thread.sleep( + 500L);

        Assertions.assertEquals(messageCountBeforeTest + 1, rabbitAdmin.getQueueInfo(AUDIT_DISCARDED_QUEUE_NAME).getMessageCount());

    }

}
