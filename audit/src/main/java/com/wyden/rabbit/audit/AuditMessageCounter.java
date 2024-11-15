package com.wyden.rabbit.audit;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuditMessageCounter {

    private final AtomicInteger inboundMessages = new AtomicInteger(0);
    private final AtomicInteger outboundMessages = new AtomicInteger(0);
    private final AtomicInteger certifiedMessages = new AtomicInteger(0);
    private final AtomicInteger discardedMessages = new AtomicInteger(0);

    public void incrementInboundMessages() {
        inboundMessages.incrementAndGet();
    }

    public void incrementOutboundMessages() {
        outboundMessages.incrementAndGet();
    }

    public void incrementCertifiedMessages() {
        certifiedMessages.incrementAndGet();
    }

    public void incrementDiscardedMessages() {
        discardedMessages.incrementAndGet();
    }

    public MessagesStats getMessagesStats() {
        return new MessagesStats(
                inboundMessages.get(),
                outboundMessages.get(),
                certifiedMessages.get(),
                discardedMessages.get());
    }

    public static record MessagesStats(
            int inboundMessages,
            int outboundMessages,
            int certifiedMessages,
            int discardedMessages) {

        public String toString() {
            return String.format("INBOUND: %d, OUTBOUND: %d, CERTIFIED: %d, DISCARDED: %d",
                    inboundMessages,
                    outboundMessages,
                    certifiedMessages,
                    discardedMessages);
        }
    }

}
