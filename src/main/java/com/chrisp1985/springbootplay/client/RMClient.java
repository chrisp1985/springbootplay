package com.chrisp1985.springbootplay.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Stands in for a third-party "ratings model" API: a slow network call that returns an
 * up-to-date rating for a player. Simulated with a blocking delay and jitter around the
 * player's current rating rather than a real HTTP call, so the enrichment flow that depends
 * on it stays runnable without external credentials or network access.
 */
@Component
public class RMClient {

    private static final Logger log = LoggerFactory.getLogger(RMClient.class);

    public double fetchLatestRating(String playerName, double currentRating) {
        log.info("Calling external ratings provider for {}", playerName);
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        double jitter = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
        double updated = Math.max(0.0, Math.min(10.0, currentRating + jitter));
        log.info("Ratings provider returned {} for {}", updated, playerName);
        return updated;
    }
}
