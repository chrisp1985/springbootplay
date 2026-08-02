package com.chrisp1985.springbootplay.repository;

import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @EnableCaching lives on the main application class, so this slice picks it up too, but
 * @DataJpaTest doesn't autoconfigure a CacheManager (it's not JPA related) — without this
 * import, context startup fails looking for one.
 */
@ImportAutoConfiguration(CacheAutoConfiguration.class)
@DataJpaTest
@ActiveProfiles("test")
class PlayerRespositoryTest {

    @Autowired
    private PlayerRespository playerRespository;

    @Test
    void findFirstByName_returnsSavedPlayer() {
        playerRespository.save(new FullPlayer(null, "Chris", 41, Position.MF, 8.9));

        Optional<FullPlayer> found = playerRespository.findFirstByName("Chris");

        assertThat(found).isPresent();
        assertThat(found.get().getAge()).isEqualTo(41);
        assertThat(found.get().getPosition()).isEqualTo(Position.MF);
    }

    @Test
    void findFirstByName_returnsEmptyWhenNoMatch() {
        Optional<FullPlayer> found = playerRespository.findFirstByName("Unknown");

        assertThat(found).isEmpty();
    }

    @Test
    void findAllByName_returnsEveryMatchingPlayer() {
        playerRespository.save(new FullPlayer(null, "TRIALIST", 19, Position.MF, 6.0));
        playerRespository.save(new FullPlayer(null, "TRIALIST", 21, Position.FW, 6.5));

        List<FullPlayer> found = playerRespository.findAllByName("TRIALIST");

        assertThat(found).hasSize(2);
    }

    /**
     * Rating refresh and AI-summary enrichment race in production (one from the scheduled
     * sweep, one from an in-flight AI call), each loading, mutating, and persisting this row
     * independently. A plain save()/merge() of a full entity loaded before the other flow's
     * write would silently clobber whichever column it didn't touch back to a stale value.
     * updateRating/updateAiSummary are narrow, single-purpose column updates specifically so
     * that can't happen — this asserts that guarantee regardless of call order.
     */
    @Test
    void updateRatingAndUpdateAiSummary_dontClobberEachOther() {
        FullPlayer saved = playerRespository.save(new FullPlayer(null, "Chris", 41, Position.MF, 8.9));

        playerRespository.updateAiSummary(saved.getId(), "Goals: 10, Assists: 5, Appearances: 20");
        playerRespository.updateRating(saved.getId(), 9.1, Instant.now());

        FullPlayer reloaded = playerRespository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getRating()).isEqualTo(9.1);
        assertThat(reloaded.getAiSummary()).isEqualTo("Goals: 10, Assists: 5, Appearances: 20");
    }
}
