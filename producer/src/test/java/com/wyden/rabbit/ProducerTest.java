package com.wyden.rabbit;

import com.wyden.rabbit.producer.Producer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.wyden.rabbit.RabbitConfig.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@TestPropertySource(properties = "app.scheduling.enable=false")
public class ProducerTest {

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    @Autowired
    @Qualifier("certifiedResultExchange")
    FanoutExchange exchangeCertified;

    @Autowired
    @Qualifier("discardedResultExchange")
    FanoutExchange exchangeDiscarded;

    @Autowired
    @Qualifier("workInboundExchange")
    FanoutExchange exchangeInbound;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @SpyBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Producer producer;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitContainer::getAdminPassword);
    }

    @Test
    void messageProduce() throws InterruptedException {

        int inboundMessagesBeforeTest = rabbitAdmin.getQueueInfo(AUDIT_INBOUND_QUEUE_NAME).getMessageCount();

        producer.setProducingEnabled(true);
        producer.produce();

        Thread.sleep(500L);
        Assertions.assertEquals(inboundMessagesBeforeTest + 1,
                rabbitAdmin.getQueueInfo(AUDIT_INBOUND_QUEUE_NAME).getMessageCount());

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(exchangeInbound.getName()), anyString(), anyString(), any(MessagePostProcessor.class));

    }

    @Test
    void messageCertify() throws InterruptedException {
        int certifiedMessagesBeforeTest =  rabbitAdmin.getQueueInfo(AUDIT_CERTIFIED_QUEUE_NAME).getMessageCount();

        rabbitTemplate.convertAndSend(WORK_OUTBOUND_EXCHANGE_NAME, "", "INBOUND_MESSAGE");

        Thread.sleep(500L);
        Assertions.assertEquals(certifiedMessagesBeforeTest + 1, rabbitAdmin.getQueueInfo(AUDIT_CERTIFIED_QUEUE_NAME).getMessageCount());

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(exchangeCertified.getName()), anyString(), anyString());
    }

}
