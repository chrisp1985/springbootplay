package com.chrisp1985.springbootplay.model.entity;

import com.chrisp1985.springbootplay.model.Position;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="player")
public class FullPlayer implements Comparable<FullPlayer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYERNO")
    private Long id;

    private String name;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Position position;

    private Double rating;

    @Column(name = "AI_SUMMARY")
    private String aiSummary;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

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
    public String getAiSummary() { return aiSummary; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setRating(Double rating) { this.rating = rating; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Natural ordering is highest rating first, since "best player first" is the default view
     * of a player list. Multi-field or ascending sorts are expressed as {@link java.util.Comparator}s
     * by callers (see {@code PlayerService#resolveComparator}) rather than folded in here.
     */
    @Override
    public int compareTo(FullPlayer other) {
        return Double.compare(other.rating, this.rating);
    }
}
