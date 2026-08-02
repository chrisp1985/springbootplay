package com.chrisp1985.springbootplay.model.entity;

import com.chrisp1985.springbootplay.model.Position;
import jakarta.persistence.*;

@Entity
@Table(name="player")
public class FullPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYERNO")
    private Long id;

    private String name;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Position position;

    private Double rating;

    protected FullPlayer() {}

    public FullPlayer(Long id, String name, Integer age, Position position, Double rating) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.position = position;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getAge() { return age; }
    public Position getPosition() { return position; }
    public Double getRating() { return rating; }
}
