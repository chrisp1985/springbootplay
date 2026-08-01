package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.service.ThreadTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/details")
public class ThreadTestController {

    private ThreadTestService threadTestService;

    public ThreadTestController(ThreadTestService threadTestService) {
        this.threadTestService = threadTestService;
    }

    @GetMapping(value = "/async")
    public ResponseEntity<String> startAsyncCheck() {
        threadTestService.checkWhenAsyncRuns();
        return ResponseEntity.ok("Finished Async Check.");
    }
}
