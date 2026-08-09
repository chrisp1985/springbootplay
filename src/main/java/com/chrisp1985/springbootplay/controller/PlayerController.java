package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.model.PlayerAiSummary;
import com.chrisp1985.springbootplay.model.PlayerResponse;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.PlayerDetailsRequest;
import com.chrisp1985.springbootplay.model.mapper.PlayerMapper;
import com.chrisp1985.springbootplay.service.PlayerEnrichmentService;
import com.chrisp1985.springbootplay.service.PlayerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;
    private final PlayerEnrichmentService playerEnrichmentService;

    @Autowired
    public PlayerController(PlayerService playerService, PlayerEnrichmentService playerEnrichmentService) {
        this.playerService = playerService;
        this.playerEnrichmentService = playerEnrichmentService;
    }

    @GetMapping
    public ResponseEntity<Page<FullPlayer>> fetchAllPlayers(Pageable pageable) {
        return ResponseEntity.ok(playerService.getAllPlayers(pageable));
    }

    @GetMapping("/{name}")
    public ResponseEntity<FullPlayer> fetchPlayerByName(@PathVariable String name) {
        return ResponseEntity.ok(playerService.getPlayerByName(name));
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> addPlayerToDb(@RequestBody @Valid PlayerRequest playerDto) {
        log.info("Received player: {}", playerDto);
        FullPlayer player = playerService.addPlayerToDatabase(playerDto);
        playerEnrichmentService.enrichPlayer(player.getName());
        PlayerResponse response = new PlayerResponse(player.getName(), player.getAge(), player.getPosition(), player.getRating());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{name}/enrich")
    public ResponseEntity<String> enrichPlayer(@PathVariable String name) {
        playerEnrichmentService.enrichPlayer(name);
        return ResponseEntity.ok(String.format("Enrichment started for: %s.", name));
    }

    @PostMapping(value = "/aidetails")
    public ResponseEntity<PlayerAiSummary> getPlayerDetailsFromAi(@RequestBody @Valid PlayerDetailsRequest player) {
        return ResponseEntity.ok(new PlayerAiSummary(player.name(), playerService.getPlayerAiInfo(player)));
    }
}
