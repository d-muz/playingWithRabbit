package com.wyden.rabbit.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WorkerController {

    private final Worker worker;

    @PostMapping(value = "timeDelayInSeconds")
    public void setWorkerTimeout(@RequestBody Integer timeDelayInSeconds) {
        worker.setTimeDelayInSeconds(timeDelayInSeconds);
        log.info("TimeDelay changed to: {}", timeDelayInSeconds);
    }

    @PostMapping(value = "exceptionThrowing")
    public void setWorkerExceptionThrowing(@RequestBody boolean exceptionThrowingState) {
        worker.setExceptionThrowingIsActive(exceptionThrowingState);
        log.info("Exception throwing changed to: {}", exceptionThrowingState);
    }

    @PostMapping(value = "exceptionRatio")
    public void setWorkerExceptionRatio(@RequestBody Integer exceptionRatio) {
        worker.setExceptionRatio(exceptionRatio);
        log.info("ExceptionRatio changed to: {}", exceptionRatio);
    }
}