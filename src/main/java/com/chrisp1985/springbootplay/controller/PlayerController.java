package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.model.Player;
import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.service.PlayerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player")
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<Player> getPlayer() {
        Player chris = new Player("Chris", 41, Position.MF, 8.9);
        log.info("Returning Player.");
        return ResponseEntity.ok(chris);
    }

    @PostMapping
    public ResponseEntity<String> addPlayer(@RequestBody @Valid Player player) {
        String playerName = playerService.returnPlayerName(player);
        log.info("Received player: {}", player);
        return ResponseEntity.ok(String.format("Added player: %s.", playerName));
    }

}
