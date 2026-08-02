package com.chrisp1985.springbootplay.health;

import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerDataHealthIndicatorTest {

    @Mock
    private PlayerRespository playerRespository;

    @Test
    void health_isUpWhenPlayersExist() {
        when(playerRespository.count()).thenReturn(5L);

        Health health = new PlayerDataHealthIndicator(playerRespository).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("players", 5L);
    }

    @Test
    void health_isDownWhenNoPlayers() {
        when(playerRespository.count()).thenReturn(0L);

        Health health = new PlayerDataHealthIndicator(playerRespository).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void health_isDownWhenRepositoryThrows() {
        when(playerRespository.count()).thenThrow(new RuntimeException("db unreachable"));

        Health health = new PlayerDataHealthIndicator(playerRespository).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
