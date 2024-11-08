package com.wyden.rabbit;

import com.wyden.rabbit.worker.Worker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.RabbitConverterFuture;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.ExecutionException;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
public class WorkerTest {

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    @Autowired
    private AsyncRabbitTemplate asyncRabbitTemplate;

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

        Queue outbound = new Queue("test-outbound");
        rabbitAdmin.declareQueue(outbound);
        rabbitAdmin.declareBinding(BindingBuilder.bind(outbound).to(exchange));

        RabbitConverterFuture<String> future = this.asyncRabbitTemplate.convertSendAndReceive("work-inbound", "", inboundMessage);
        String result = future.get();

        Assertions.assertEquals(inboundMessage + "-processed", result);
        Assertions.assertEquals(1, rabbitAdmin.getQueueInfo("test-outbound").getMessageCount());

    }

}
