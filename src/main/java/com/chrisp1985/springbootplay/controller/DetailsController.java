package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.service.DetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/details")
public class DetailsController {

    private DetailsService detailsService;

    public DetailsController(DetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @GetMapping(value = "/async")
    public ResponseEntity<String> startAsyncCheck() {
        detailsService.checkWhenAsyncRuns();
        return ResponseEntity.ok("Finished Async Check.");
    }
}
