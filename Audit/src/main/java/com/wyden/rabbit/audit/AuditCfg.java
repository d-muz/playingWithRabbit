package com.wyden.rabbit.audit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditCfg {

    public static final String AUDIT_INBOUND_QUEUE_NAME = "audit-inbound";
    public static final String AUDIT_OUTBOUND_QUEUE_NAME = "audit-outbound";
    public static final String AUDIT_CERTIFIED_QUEUE_NAME = "audit-certified";
    public static final String AUDIT_DISCARDED_QUEUE_NAME = "discarded-certified";

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
    public Queue inboundQueue() {
        return new Queue(AUDIT_INBOUND_QUEUE_NAME);
    }

    @Bean
    public Queue outboundQueue() {
        return new Queue(AUDIT_OUTBOUND_QUEUE_NAME);
    }

    @Bean
    public Queue certifiedQueue() {
        return new Queue(AUDIT_CERTIFIED_QUEUE_NAME);
    }

    @Bean
    public Queue discardedQueue() {
        return new Queue(AUDIT_DISCARDED_QUEUE_NAME);
    }


    @Bean
    public Binding bindingInbound() {
        return BindingBuilder
                .bind(inboundQueue())
                .to(workInboundExchange());
    }

    @Bean
    public Binding bindingOutbound() {
        return BindingBuilder
                .bind(outboundQueue())
                .to(workOutboundExchange());
    }

    @Bean
    public Binding bindingCertified() {
        return BindingBuilder
                .bind(certifiedQueue())
                .to(certifiedResultExchange());
    }

    @Bean
    public Binding bindingDiscarded() {
        return BindingBuilder
                .bind(discardedQueue())
                .to(discardedResultExchange());
    }
}
