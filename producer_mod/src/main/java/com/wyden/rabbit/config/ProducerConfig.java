package com.wyden.rabbit.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProducerConfig {

    @Bean
    public FanoutExchange workInboundExchange() {
        return new FanoutExchange("work-inbound");
    }

    @Bean
    public FanoutExchange workOutboundExchange() {
        return new FanoutExchange("work-outbound");
    }

    @Bean
    public FanoutExchange certifiedResultExchange() {
        return new FanoutExchange("certified-result");
    }

    @Bean
    public FanoutExchange discardedResultExchange() {
        return new FanoutExchange("discarded-result");
    }

    @Bean
    public AsyncRabbitTemplate asyncRabbitTemplate(RabbitTemplate rabbitTemplate){
        return new AsyncRabbitTemplate(rabbitTemplate);
    }

}
