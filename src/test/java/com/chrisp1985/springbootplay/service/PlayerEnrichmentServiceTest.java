package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.client.RMClient;
import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.AuditAction;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerEnrichmentServiceTest {

    @Mock
    private PlayerRespository playerRespository;
    @Mock
    private AuditLogRespository auditLogRespository;
    @Mock
    private RMClient rmClient;
    @Mock
    private PlayerService playerService;

    private PlayerEnrichmentService playerEnrichmentService;

    @Test
    void refreshRating_updatesPlayerAndWritesAuditEntry() {
        playerEnrichmentService = new PlayerEnrichmentService(playerRespository, auditLogRespository, rmClient, playerService);
        FullPlayer player = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerRespository.findAllByName("Chris")).thenReturn(List.of(player));
        when(rmClient.fetchLatestRating("Chris", 8.9)).thenReturn(9.1);

        playerEnrichmentService.refreshRating("Chris");

        verify(playerRespository).updateRating(eq(1L), eq(9.1), any(Instant.class));

        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRespository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo(AuditAction.RATING_REFRESH);
        assertThat(auditCaptor.getValue().getRating()).isEqualTo(9.1);
    }

    @Test
    void refreshRating_doesNothingWhenPlayerNotFound() {
        playerEnrichmentService = new PlayerEnrichmentService(playerRespository, auditLogRespository, rmClient, playerService);
        when(playerRespository.findAllByName("Unknown")).thenReturn(List.of());

        playerEnrichmentService.refreshRating("Unknown");

        verify(auditLogRespository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void enrichPlayer_refreshesRatingAndStoresAiSummary() {
        playerEnrichmentService = new PlayerEnrichmentService(playerRespository, auditLogRespository, rmClient, playerService);
        FullPlayer player = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerRespository.findAllByName("Chris")).thenReturn(List.of(player));
        when(rmClient.fetchLatestRating(eq("Chris"), anyDouble())).thenReturn(9.0);
        when(playerService.getPlayerAiInfo(any())).thenReturn("Goals: 10, Assists: 5, Appearances: 20");

        playerEnrichmentService.enrichPlayer("Chris");

        verify(playerRespository).updateAiSummary(1L, "Goals: 10, Assists: 5, Appearances: 20");
        verify(auditLogRespository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void refreshAllRatings_refreshesEveryPlayer() {
        playerEnrichmentService = new PlayerEnrichmentService(playerRespository, auditLogRespository, rmClient, playerService);
        FullPlayer chris = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        FullPlayer alex = new FullPlayer(2L, "Alex", 25, Position.FW, 7.2);
        when(playerRespository.findAll()).thenReturn(List.of(chris, alex));
        when(playerRespository.findAllByName(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return name.equals("Chris") ? List.of(chris) : List.of(alex);
        });
        when(rmClient.fetchLatestRating(anyString(), anyDouble())).thenReturn(8.0);

        playerEnrichmentService.refreshAllRatings();

        verify(rmClient).fetchLatestRating(eq("Chris"), anyDouble());
        verify(rmClient).fetchLatestRating(eq("Alex"), anyDouble());
    }
}
