package com.wyden.rabbit.audit;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuditController {

    private final AuditMessageCounter counter;

    @Operation(summary = "Get message processing statistics")
    @GetMapping("stats")
    public AuditMessageCounter.MessagesStats getMessageStats() {
        return counter.getMessagesStats();
    }
}
