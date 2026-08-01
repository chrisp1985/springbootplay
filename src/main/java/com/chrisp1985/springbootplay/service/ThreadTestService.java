package com.chrisp1985.springbootplay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ThreadTestService {

    private static final Logger log = LoggerFactory.getLogger(ThreadTestService.class);

    private WorkersService workersService;

    public ThreadTestService(WorkersService workersService) {
        this.workersService = workersService;
    }


    public void startWorkers() {
        log.info("Started StartWorkers Function.");
        workersService.runExternalWorkers();
        log.info("Finished StartWorkers Function.");
    }

    /**
     * Scheduled runs at a fixed rate, like below.
     */
    @Scheduled(fixedRate = 5000L)
    public void scheduledCheck() {
        log.info("Checking...");
    }

    /**
     * As runExternalWorkers is an Async method, the transactional method completes as soon as runExternalWorkers is kicked off.
     */
    public void checkWhenAsyncRuns() {
        workersService.longRunningWorker("Trial", 10000L);
        workersService.runExternalWorkers();
    }
}

