package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.exception.PlayerNotFoundException;
import com.chrisp1985.springbootplay.model.PlayerDetailsRequest;
import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.AuditAction;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.model.mapper.AuditMapper;
import com.chrisp1985.springbootplay.model.mapper.PlayerMapper;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        playerService = new PlayerService(chatClientBuilder, playerMapper, auditMapper, playerRespository, auditLogRespository, meterRegistry);
    }

    @Test
    void addPlayerToDatabase_savesPlayerAndAuditEntry() {
        PlayerRequest request = new PlayerRequest("Chris", 41, Position.MF, 8.9);
        FullPlayer entity = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        AuditLog auditEntity = new AuditLog(null, "Chris", AuditAction.CREATED, 8.9, Instant.now());

        when(playerMapper.toEntity(request)).thenReturn(entity);
        when(playerRespository.save(entity)).thenReturn(entity);
        when(auditMapper.toEntity(request)).thenReturn(auditEntity);

        FullPlayer result = playerService.addPlayerToDatabase(request);

        assertThat(result.getName()).isEqualTo("Chris");
        verify(auditLogRespository).save(auditEntity);
    }

    @Test
    void getPlayerByName_returnsExistingPlayer() {
        FullPlayer entity = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerRespository.findFirstByName("Chris")).thenReturn(Optional.of(entity));

        FullPlayer result = playerService.getPlayerByName("Chris");

        assertThat(result.getName()).isEqualTo("Chris");
    }

    @Test
    void getPlayerByName_throwsWhenNotFound() {
        when(playerRespository.findFirstByName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerByName("Unknown"))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void getAllPlayers_returnsPageOfPlayersFromRepository() {
        FullPlayer entity = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        Pageable pageable = PageRequest.of(0, 20);
        when(playerRespository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));

        Page<FullPlayer> result = playerService.getAllPlayers(pageable);

        assertThat(result.getContent()).containsExactly(entity);
    }

    @Test
    void getPlayerAiInfo_returnsChatClientContent() {
        ChatClient deepChatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(deepChatClient);
        PlayerService service = new PlayerService(chatClientBuilder, playerMapper, auditMapper, playerRespository, auditLogRespository, meterRegistry);
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("Goals: 10, Assists: 5, Appearances: 20"))));
        when(deepChatClient.prompt(anyString()).options(any()).call().chatResponse()).thenReturn(chatResponse);

        String result = service.getPlayerAiInfo(new PlayerDetailsRequest("Chris"));

        assertThat(result).isEqualTo("Goals: 10, Assists: 5, Appearances: 20");
    }

    /**
     * Reproduces the actual bug seen manually against the real API: when thinking is on, the
     * Anthropic client can return the thinking block and the text answer as two separate
     * Generations. getPlayerAiInfo must pull text from every generation and skip the blank
     * one, not just take chatResponse().getResult() (the first generation).
     */
    @Test
    void getPlayerAiInfo_skipsBlankGenerationsAndUsesTheOneWithText() {
        ChatClient deepChatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(deepChatClient);
        PlayerService service = new PlayerService(chatClientBuilder, playerMapper, auditMapper, playerRespository, auditLogRespository, meterRegistry);
        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("")),
                new Generation(new AssistantMessage("Goals: 10, Assists: 5, Appearances: 20"))));
        when(deepChatClient.prompt(anyString()).options(any()).call().chatResponse()).thenReturn(chatResponse);

        String result = service.getPlayerAiInfo(new PlayerDetailsRequest("Chris"));

        assertThat(result).isEqualTo("Goals: 10, Assists: 5, Appearances: 20");
    }

    @Test
    void addPlayerToDatabase_recordsDbLatencyMetricForEachRepositoryCall() {
        PlayerRequest request = new PlayerRequest("Chris", 41, Position.MF, 8.9);
        FullPlayer entity = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        AuditLog auditEntity = new AuditLog(null, "Chris", AuditAction.CREATED, 8.9, Instant.now());

        when(playerMapper.toEntity(request)).thenReturn(entity);
        when(playerRespository.save(entity)).thenReturn(entity);
        when(auditMapper.toEntity(request)).thenReturn(auditEntity);

        playerService.addPlayerToDatabase(request);

        assertThat(meterRegistry.get("db.latency").tag("operation", "save").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.get("db.latency").tag("operation", "auditSave").timer().count()).isEqualTo(1);
    }

    @Test
    void getPlayerByName_recordsDbLatencyMetric() {
        when(playerRespository.findFirstByName("Chris")).thenReturn(Optional.of(new FullPlayer(1L, "Chris", 41, Position.MF, 8.9)));

        playerService.getPlayerByName("Chris");

        assertThat(meterRegistry.get("db.latency").tag("operation", "findByName").timer().count()).isEqualTo(1);
    }
}
