package com.wyden.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String AUDIT_INBOUND_QUEUE_NAME = "audit-inbound";
    public static final String AUDIT_OUTBOUND_QUEUE_NAME = "audit-outbound";
    public static final String AUDIT_CERTIFIED_QUEUE_NAME = "audit-certified";
    public static final String AUDIT_DISCARDED_QUEUE_NAME = "audit-discarded";
    public static final String WORKER_INBOUND_QUEUE_NAME = "worker-inbound";
    public static final String WORKER_OUTBOUND_QUEUE_NAME = "worker-outbound";

    public static final int MESSAGE_TTL_IN_MILLIS = 10000;
    public static final String DEAD_LETTER_EXCHANGE_NAME = "discarded-result";
    public static final String WORK_INBOUND_EXCHANGE_NAME = "work-inbound";
    public static final String WORK_OUTBOUND_EXCHANGE_NAME = "work-outbound";
    public static final String CERTIFIED_RESULT_EXCHANGE_NAME = "certified-result";


    @Bean
    public FanoutExchange workInboundExchange() {
        return new FanoutExchange(WORK_INBOUND_EXCHANGE_NAME);
    }

    @Bean
    public FanoutExchange workOutboundExchange() {
        return new FanoutExchange(WORK_OUTBOUND_EXCHANGE_NAME);
    }

    @Bean
    public FanoutExchange certifiedResultExchange() {
        return new FanoutExchange(CERTIFIED_RESULT_EXCHANGE_NAME);
    }

    @Bean
    public FanoutExchange discardedResultExchange() {
        return new FanoutExchange(DEAD_LETTER_EXCHANGE_NAME);
    }


    @Bean
    public Queue inboundAuditQueue() {
        return new Queue(AUDIT_INBOUND_QUEUE_NAME);
    }

    @Bean
    public Queue inboundWorkerQueue() {
        return QueueBuilder.durable(WORKER_INBOUND_QUEUE_NAME)
                .ttl(MESSAGE_TTL_IN_MILLIS)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
                .deadLetterRoutingKey("")
                .build();
    }

    @Bean
    public Queue outboundAuditQueue() {
        return new Queue(AUDIT_OUTBOUND_QUEUE_NAME);
    }

    @Bean
    public Queue outboundWorkerQueue() {
        return new Queue(WORKER_OUTBOUND_QUEUE_NAME);
    }

    @Bean
    public Queue certifiedAuditQueue() {
        return new Queue(AUDIT_CERTIFIED_QUEUE_NAME);
    }

    @Bean
    public Queue discardedAuditQueue() {
        return new Queue(AUDIT_DISCARDED_QUEUE_NAME);
    }


    @Bean
    public Binding bindingAuditInbound() {
        return BindingBuilder
                .bind(inboundAuditQueue())
                .to(workInboundExchange());
    }

    @Bean
    public Binding bindingWorkerInbound() {
        return BindingBuilder
                .bind(inboundWorkerQueue())
                .to(workInboundExchange());
    }

    @Bean
    public Binding bindingAuditOutbound() {
        return BindingBuilder
                .bind(outboundAuditQueue())
                .to(workOutboundExchange());
    }

    @Bean
    public Binding bindingWorkerOutbound() {
        return BindingBuilder
                .bind(outboundWorkerQueue())
                .to(workOutboundExchange());
    }

    @Bean
    public Binding bindingAuditCertified() {
        return BindingBuilder
                .bind(certifiedAuditQueue())
                .to(certifiedResultExchange());
    }

    @Bean
    public Binding bindingAuditDiscarded() {
        return BindingBuilder
                .bind(discardedAuditQueue())
                .to(discardedResultExchange());
    }
}
