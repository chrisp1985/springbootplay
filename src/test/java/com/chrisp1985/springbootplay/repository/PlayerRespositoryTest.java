package com.chrisp1985.springbootplay.repository;

import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PlayerRespositoryTest {

    @Autowired
    private PlayerRespository playerRespository;

    @Test
    void findByName_returnsSavedPlayer() {
        playerRespository.save(new FullPlayer(null, "Chris", 41, Position.MF, 8.9));

        Optional<FullPlayer> found = playerRespository.findByName("Chris");

        assertThat(found).isPresent();
        assertThat(found.get().getAge()).isEqualTo(41);
        assertThat(found.get().getPosition()).isEqualTo(Position.MF);
    }

    @Test
    void findByName_returnsEmptyWhenNoMatch() {
        Optional<FullPlayer> found = playerRespository.findByName("Unknown");

        assertThat(found).isEmpty();
    }
}
