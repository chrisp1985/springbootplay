package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.PlayerDetailsRequest;
import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.service.PlayerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<PlayerRequest> fetchManualChris() {
        PlayerRequest chris = new PlayerRequest("Chris", 41, Position.MF, 8.9);
        log.info("Returning Player.");
        return ResponseEntity.ok(chris);
    }

    @PostMapping
    public ResponseEntity<String> addPlayerToDb(@RequestBody @Valid PlayerRequest playerDto) {
        log.info("Received player: {}", playerDto);
        FullPlayer player = playerService.addPlayerToDatabase(playerDto);
        return ResponseEntity.ok(String.format("Added player: %s.", player.getName()));
    }

    @PostMapping(value = "/aidetails")
    public ResponseEntity<String> getPlayerDetailsFromAi(@RequestBody @Valid PlayerDetailsRequest player) {
        return ResponseEntity.ok(playerService.getPlayerAiInfo(player));
    }

    @GetMapping("/database")
    public ResponseEntity<String> getPlayerFromDatabase(@RequestBody @Valid PlayerDetailsRequest player) {
        return ResponseEntity.ok(playerService.getPlayerDatabaseInfo(player.name()));
    }
}
