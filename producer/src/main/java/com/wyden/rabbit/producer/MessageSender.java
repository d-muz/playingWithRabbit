package com.wyden.rabbit.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MessageSender <T>{

    private final AsyncRabbitTemplate asyncRabbitTemplate;

    public MessageSender(AsyncRabbitTemplate asyncRabbitTemplate) {
        this.asyncRabbitTemplate = asyncRabbitTemplate;
    }

    public CompletableFuture<T> sendMessage(String exchangeName, String routingKey, String message){
        return this.asyncRabbitTemplate.convertSendAndReceive(exchangeName, "", message);
    }
}
