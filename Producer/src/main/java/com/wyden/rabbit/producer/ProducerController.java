package com.wyden.rabbit.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProducerController {

    private final Producer producer;

    @PostMapping(value = "runningState")
    public void setProducerStatus(@RequestBody boolean state) {
        producer.setProducingEnabled(state);
        log.info("Producer state changed to: {}", producer.isProducingEnabled());
    }

}
