package com.wyden.rabbit.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.wyden.rabbit.RabbitConfig.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class Audit {

    private final AuditMessageCounter counter;

    @RabbitListener(queues = AUDIT_INBOUND_QUEUE_NAME)
    public void auditInbound(String message) {
        log.info("INBOUND: {}", message);
        counter.incrementInboundMessages();
        logStats();
    }

    @RabbitListener(queues = AUDIT_OUTBOUND_QUEUE_NAME)
    public void auditOutbound(String message) {
        log.info("OUTBOUND: {}", message);
        counter.incrementOutboundMessages();
        logStats();
    }

    @RabbitListener(queues = AUDIT_CERTIFIED_QUEUE_NAME)
    public void auditCertified(String message) {
        log.info("CERTIFIED: {}", message);
        counter.incrementCertifiedMessages();
        logStats();
    }

    @RabbitListener(queues = AUDIT_DISCARDED_QUEUE_NAME)
    public void auditDiscarded(String message) {
        log.info("DISCARDED: {}", message);
        counter.incrementDiscardedMessages();
        logStats();
    }

    private void logStats() {
        log.info("STATS: {}", counter.getMessagesStats().toString());
    }
}
