package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.model.*;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.model.mapper.AuditMapper;
import com.chrisp1985.springbootplay.model.mapper.PlayerMapper;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private final ChatClient chatClient;
    private final PlayerRespository playerRespository;
    private final AuditLogRespository auditLogRespository;
    private final PlayerMapper playerMapper;
    private final AuditMapper auditMapper;
    private final MeterRegistry meterRegistry;

    @Autowired
    public PlayerService(ChatClient.Builder chatClientBuilder, PlayerMapper playerMapper,
                         AuditMapper auditMapper, PlayerRespository playerRespository, AuditLogRespository auditLogRespository,
                         MeterRegistry meterRegistry) {
        this.chatClient = chatClientBuilder.build();
        this.playerMapper = playerMapper;
        this.playerRespository = playerRespository;
        this.auditLogRespository = auditLogRespository;
        this.auditMapper = auditMapper;
        this.meterRegistry = meterRegistry;
    }

    public String returnPlayerName(PlayerRequest playerDto) {
        return playerDto.name();
    }

    public String getPlayerAiInfo(PlayerDetailsRequest player) {
        return chatClient
                .prompt("Return the current season football stats for " + player.name() +
                        " as plain text. Include goals, assists, appearances.")
                .call()
                .content();
    }

    public String getPlayerDatabaseInfo(String name) {
        return timeDbCall("findByName", () -> playerRespository.findByName(name))
                .orElseGet(() -> playerMapper.toEntity(new PlayerRequest("default", 23, Position.MF, 0.0)))
                .getName();
    }

    @Transactional
    public FullPlayer addPlayerToDatabase(PlayerRequest playerDto) {
        FullPlayer addedPlayer = timeDbCall("save", () -> playerRespository.save(playerMapper.toEntity(playerDto)));
        log.info("Saving data: {}", playerDto);
        timeDbCall("auditSave", () -> auditLogRespository.save(auditMapper.toEntity(playerDto)));
        return addedPlayer;
    }

    /**
     * Wraps a repository call with a `db.latency` timer, tagged by operation, so the round trip
     * from this service to the database shows up in /actuator/metrics rather than only being
     * visible in a profiler.
     */
    private <T> T timeDbCall(String operation, Supplier<T> call) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return call.get();
        } finally {
            sample.stop(Timer.builder("db.latency")
                    .description("Latency of calls from PlayerService to the database")
                    .tag("operation", operation)
                    .register(meterRegistry));
        }
    }
}
