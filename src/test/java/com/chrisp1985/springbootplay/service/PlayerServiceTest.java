package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.model.PlayerDetailsRequest;
import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.model.mapper.AuditMapper;
import com.chrisp1985.springbootplay.model.mapper.PlayerMapper;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private PlayerRespository playerRespository;
    @Mock
    private AuditLogRespository auditLogRespository;
    @Mock
    private PlayerMapper playerMapper;
    @Mock
    private AuditMapper auditMapper;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        playerService = new PlayerService(chatClientBuilder, playerMapper, auditMapper, playerRespository, auditLogRespository);
    }

    @Test
    void returnPlayerName_returnsNameFromRequest() {
        PlayerRequest request = new PlayerRequest("Chris", 41, Position.MF, 8.9);

        assertThat(playerService.returnPlayerName(request)).isEqualTo("Chris");
    }

    @Test
    void addPlayerToDatabase_savesPlayerAndAuditEntry() {
        PlayerRequest request = new PlayerRequest("Chris", 41, Position.MF, 8.9);
        FullPlayer entity = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        AuditLog auditEntity = new AuditLog(null, "Chris", 41, Position.MF, 8.9);

        when(playerMapper.toEntity(request)).thenReturn(entity);
        when(playerRespository.save(entity)).thenReturn(entity);
        when(auditMapper.toEntity(request)).thenReturn(auditEntity);

        FullPlayer result = playerService.addPlayerToDatabase(request);

        assertThat(result.getName()).isEqualTo("Chris");
        verify(auditLogRespository).save(auditEntity);
    }

    @Test
    void getPlayerDatabaseInfo_returnsExistingPlayerName() {
        FullPlayer entity = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerRespository.findByName("Chris")).thenReturn(Optional.of(entity));

        String name = playerService.getPlayerDatabaseInfo("Chris");

        assertThat(name).isEqualTo("Chris");
    }

    @Test
    void getPlayerDatabaseInfo_fallsBackToDefaultPlayerWhenNotFound() {
        FullPlayer defaultEntity = new FullPlayer(null, "default", 23, Position.MF, 0.0);
        when(playerRespository.findByName("Unknown")).thenReturn(Optional.empty());
        when(playerMapper.toEntity(any(PlayerRequest.class))).thenReturn(defaultEntity);

        String name = playerService.getPlayerDatabaseInfo("Unknown");

        assertThat(name).isEqualTo("default");
    }

    @Test
    void getPlayerAiInfo_returnsChatClientContent() {
        ChatClient deepChatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(deepChatClient);
        PlayerService service = new PlayerService(chatClientBuilder, playerMapper, auditMapper, playerRespository, auditLogRespository);
        when(deepChatClient.prompt(anyString()).call().content()).thenReturn("Goals: 10, Assists: 5, Appearances: 20");

        String result = service.getPlayerAiInfo(new PlayerDetailsRequest("Chris"));

        assertThat(result).isEqualTo("Goals: 10, Assists: 5, Appearances: 20");
    }
}
