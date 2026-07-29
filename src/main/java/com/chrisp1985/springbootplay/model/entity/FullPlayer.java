package com.chrisp1985.springbootplay.model.entity;

import com.chrisp1985.springbootplay.model.Position;
import jakarta.persistence.*;

@Entity
@Table(name="player")
public record FullPlayer(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "PLAYERNO")
        Long id,
        String name,
        Integer age,
        Position position,
        Double rating
) {
}
