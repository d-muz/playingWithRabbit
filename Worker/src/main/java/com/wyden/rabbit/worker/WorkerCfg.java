package com.wyden.rabbit.worker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerCfg {
    public static final String WORKER_INBOUND_QUEUE_NAME = "worker-inbound";

    @Bean
    public FanoutExchange workInboundExchange() {
        return new FanoutExchange("work-inbound");
    }

    @Bean
    public FanoutExchange workOutboundExchange() {
        return new FanoutExchange("work-outbound");
    }

    @Bean
    public Queue inboundQueue() {
        return new Queue(WORKER_INBOUND_QUEUE_NAME);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(inboundQueue())
                .to(workInboundExchange());
    }
}
