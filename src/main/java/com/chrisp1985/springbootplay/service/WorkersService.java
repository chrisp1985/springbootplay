package com.chrisp1985.springbootplay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WorkersService {

    private static final Logger log = LoggerFactory.getLogger(WorkersService.class);

    @Async
    public void runExternalWorkers() {
        log.info("Starting External Workers.");
        shortRunningWorker("Shorty1");
        longRunningWorker("Long1", 5000L);
        shortRunningWorker("Shorty2");
        longRunningWorker("Long2", 5000L);
        log.info("Done with External Workers.");
    }

    public void longRunningWorker(String name, Long timeMs) {
        try {
            log.info("Running long running worker: {}", name);
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void shortRunningWorker(String name) {
        try {
            log.info("Running short running worker: {}", name);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
