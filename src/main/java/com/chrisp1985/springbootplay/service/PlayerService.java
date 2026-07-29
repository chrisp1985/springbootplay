package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.model.*;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private final ChatClient chatClient;
    private final PlayerRespository playerRespository;
    private final AuditLogRespository auditLogRespository;
    private final PlayerMapper playerMapper;
    private final AuditMapper auditMapper;

    @Autowired
    public PlayerService(ChatClient.Builder chatClientBuilder, PlayerMapper playerMapper,
                         AuditMapper auditMapper, PlayerRespository playerRespository, AuditLogRespository auditLogRespository) {
        this.chatClient = chatClientBuilder.build();
        this.playerMapper = playerMapper;
        this.playerRespository = playerRespository;
        this.auditLogRespository = auditLogRespository;
        this.auditMapper = auditMapper;
    }

    public String returnPlayerName(PlayerRequest playerDto) {
        return playerDto.name();
    }

    public String getPlayerInfo(PlayerDetailsRequest player) {
        return chatClient
                .prompt("Return the current season football stats for " + player.name() +
                        " as plain text. Include goals, assists, appearances.")
                .call()
                .content();
    }

    public String getPlayerDatabaseInfo(String name) {
        return playerRespository
                .findById(name)
                .orElse(playerMapper.toRequest(new PlayerRequest("default", 23, Position.MF, 0.0)))
                .name();
    }

    @Transactional
    public FullPlayer addPlayerToDatabase(PlayerRequest playerDto) {
        FullPlayer addedPlayer = playerRespository.save(playerMapper.toRequest(playerDto));
        log.info("Saving data: {}", playerDto);
        auditLogRespository.save(auditMapper.toEntity(playerDto));
        return addedPlayer;
    }
}
