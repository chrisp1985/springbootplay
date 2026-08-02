package com.chrisp1985.springbootplay.health;

import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Reports DOWN if the player table can't be reached or is empty, so the demo failure mode
 * (broken DB connection, or a fresh DB with no seed data) shows up in /actuator/health rather
 * than only surfacing when an endpoint is called.
 */
@Component("playerData")
public class PlayerDataHealthIndicator implements HealthIndicator {

    private final PlayerRespository playerRespository;

    public PlayerDataHealthIndicator(PlayerRespository playerRespository) {
        this.playerRespository = playerRespository;
    }

    @Override
    public Health health() {
        long playerCount;
        try {
            playerCount = playerRespository.count();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }

        if (playerCount == 0) {
            return Health.down()
                    .withDetail("players", 0)
                    .withDetail("reason", "No players in database")
                    .build();
        }

        return Health.up()
                .withDetail("players", playerCount)
                .build();
    }
}
