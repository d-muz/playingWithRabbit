package com.wyden.rabbit;

import com.wyden.rabbit.audit.Audit;
import com.wyden.rabbit.audit.AuditMessageCounter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.wyden.rabbit.RabbitConfig.*;

@SpringBootTest
@Testcontainers
public class AuditTest {

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AuditMessageCounter counter;

    @Autowired
    private Audit audit;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitContainer::getAdminPassword);
    }

    @Test
    void testAuditInboundCounter() throws InterruptedException {
        rabbitTemplate.convertAndSend(WORK_INBOUND_EXCHANGE_NAME, "", "INBOUND_MESSAGE");

        Thread.sleep(1000);
        AuditMessageCounter.MessagesStats messagesStats = counter.getMessagesStats();
        Assertions.assertEquals(1, messagesStats.inboundMessages());
    }

    @Test
    void testAuditOutboundCounter() throws InterruptedException {
        rabbitTemplate.convertAndSend(WORK_OUTBOUND_EXCHANGE_NAME, "", "OUTBOUND_MESSAGE");

        Thread.sleep(1000);
        AuditMessageCounter.MessagesStats messagesStats = counter.getMessagesStats();
        Assertions.assertEquals(1, messagesStats.outboundMessages());
    }

    @Test
    void testAuditCertifiedCounter() throws InterruptedException {
        rabbitTemplate.convertAndSend(CERTIFIED_RESULT_EXCHANGE_NAME, "", "CERTIFIED_MESSAGE");

        Thread.sleep(1000);
        AuditMessageCounter.MessagesStats messagesStats = counter.getMessagesStats();
        Assertions.assertEquals(1, messagesStats.certifiedMessages());
    }

    @Test
    void testAuditDiscardedCounter() throws InterruptedException {
        rabbitTemplate.convertAndSend(DEAD_LETTER_EXCHANGE_NAME, "", "DISCARDED_MESSAGE");

        Thread.sleep(1000);
        AuditMessageCounter.MessagesStats messagesStats = counter.getMessagesStats();
        Assertions.assertEquals(1, messagesStats.discardedMessages());
    }

}
