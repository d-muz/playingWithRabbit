package com.wyden.rabbit;

import com.wyden.rabbit.producer.MessageSender;
import com.wyden.rabbit.producer.Producer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@TestPropertySource(properties = "app.scheduling.enable=false")
public class ProducerTest {

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    @MockBean
    private MessageSender<String> messageSender;

    @Autowired
    @Qualifier("certifiedResultExchange")
    FanoutExchange exchangeCertified;

    @Autowired
    @Qualifier("discardedResultExchange")
    FanoutExchange exchangeDiscarded;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private AsyncRabbitTemplate asyncRabbitTemplate;

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
    void messageProduce_without_time_delay() throws InterruptedException {

        int certifiedMessagesBeforeTest = initRabbitQueueAndBinding("test-certified", exchangeCertified);
        int discardedMessagesBeforeTest = initRabbitQueueAndBinding("test-discarded", exchangeDiscarded);

        when(messageSender.sendMessage(anyString(), anyString(), anyString()))
                .thenReturn(getFuture());

        producer.produce();

        Thread.sleep(500L);
        Assertions.assertEquals(certifiedMessagesBeforeTest + 1,
                rabbitAdmin.getQueueInfo("test-certified").getMessageCount());
        Assertions.assertEquals(discardedMessagesBeforeTest,
                rabbitAdmin.getQueueInfo("test-discarded").getMessageCount());
    }

    @Test
    void messageProduce_with_time_delay_lower_than_limit() throws InterruptedException {
        int timeOutInMillis = 1000;
        producer.setDiscardMessageTimeoutInSeconds(2);

        int certifiedMessagesBeforeTest = initRabbitQueueAndBinding("test-certified", exchangeCertified);
        int discardedMessagesBeforeTest = initRabbitQueueAndBinding("test-discarded", exchangeDiscarded);

        when(messageSender.sendMessage(anyString(), anyString(), anyString()))
                .thenReturn(getAsyncFuture(timeOutInMillis));

        producer.produce();

        Thread.sleep(timeOutInMillis + 500L);
        Assertions.assertEquals(certifiedMessagesBeforeTest + 1, rabbitAdmin.getQueueInfo("test-certified").getMessageCount());
        Assertions.assertEquals(discardedMessagesBeforeTest, rabbitAdmin.getQueueInfo("test-discarded").getMessageCount());
    }

    @Test
    void messageProduce_with_time_delay_upper_than_limit() throws InterruptedException {
        int timeOutInMillis = 3000;
        producer.setDiscardMessageTimeoutInSeconds(2);

        int certifiedMessagesBeforeTest = initRabbitQueueAndBinding("test-certified", exchangeCertified);
        int discardedMessagesBeforeTest = initRabbitQueueAndBinding("test-discarded", exchangeDiscarded);

        when(messageSender.sendMessage(anyString(), anyString(), anyString()))
                .thenReturn(getAsyncFuture(4000));

        producer.produce();

        Thread.sleep(timeOutInMillis + 500L);
        Assertions.assertEquals(certifiedMessagesBeforeTest, rabbitAdmin.getQueueInfo("test-certified").getMessageCount());
        Assertions.assertEquals(discardedMessagesBeforeTest + 1, rabbitAdmin.getQueueInfo("test-discarded").getMessageCount());
    }

    private int initRabbitQueueAndBinding(String queueName, FanoutExchange exchange) {
        Queue certified = new Queue(queueName);
        rabbitAdmin.declareQueue(certified);
        rabbitAdmin.declareBinding(BindingBuilder.bind(certified).to(exchange));
        return rabbitAdmin.getQueueInfo(certified.getName()).getMessageCount();
    }

    private CompletableFuture<String> getFuture() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("value-processed");
        return future;
    }

    private CompletableFuture<String> getAsyncFuture(long millis) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return Instant.now().getEpochSecond() + "-processed";
        });
    }
}
