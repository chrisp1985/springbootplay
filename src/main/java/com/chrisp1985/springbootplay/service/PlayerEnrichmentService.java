package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.client.RMClient;
import com.chrisp1985.springbootplay.model.PlayerDetailsRequest;
import com.chrisp1985.springbootplay.model.entity.AuditAction;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Keeps player data fresh after it's first added. Refreshing a rating from the external
 * provider is a slow, blocking call ({@link RMClient}), so it's kicked off with {@link Async}
 * rather than making the caller (an add-player or manual-enrich request) wait for it.
 */
@Service
public class PlayerEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(PlayerEnrichmentService.class);

    private final PlayerRespository playerRespository;
    private final AuditLogRespository auditLogRespository;
    private final RMClient rmClient;
    private final PlayerService playerService;

    public PlayerEnrichmentService(PlayerRespository playerRespository, AuditLogRespository auditLogRespository,
                                    RMClient rmClient, PlayerService playerService) {
        this.playerRespository = playerRespository;
        this.auditLogRespository = auditLogRespository;
        this.rmClient = rmClient;
        this.playerService = playerService;
    }

    /**
     * Full enrichment: a fresh rating from the external provider plus an AI-generated season
     * summary. Triggered right after a player is added, or on demand via the manual enrich
     * endpoint.
     */
    @Async
    public void enrichPlayer(String name) {
        log.info("Starting enrichment for {}", name);
        refreshRating(name);

        playerRespository.findAllByName(name).forEach(player -> {
            String summary = playerService.getPlayerAiInfo(new PlayerDetailsRequest(name));
            playerRespository.updateAiSummary(player.getId(), summary);
            saveAuditEntry(name, player.getRating(), AuditAction.AI_ENRICHED);
        });
        log.info("Finished enrichment for {}", name);
    }

    /**
     * Rating-only refresh. Used both by the manual/full enrichment flow above and by the
     * scheduled sweep below, which deliberately skips the AI summary call so it doesn't hit
     * the Anthropic API on a timer.
     */
    @Transactional
    public void refreshRating(String name) {
        playerRespository.findAllByName(name).forEach(player -> {
            double updatedRating = rmClient.fetchLatestRating(player.getName(), player.getRating());
            playerRespository.updateRating(player.getId(), updatedRating, Instant.now());
            saveAuditEntry(name, updatedRating, AuditAction.RATING_REFRESH);
        });
    }

    /**
     * Periodically re-pulls every player's rating from the external provider, so ratings
     * don't go stale between manual enrichments.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000L)
    public void refreshAllRatings() {
        List<FullPlayer> players = playerRespository.findAll();
        log.info("Scheduled rating sweep starting for {} player(s)", players.size());
        players.forEach(player -> refreshRating(player.getName()));
    }

    private void saveAuditEntry(String name, Double rating, AuditAction action) {
        auditLogRespository.save(new AuditLog(null, name, action, rating, Instant.now()));
    }
}
