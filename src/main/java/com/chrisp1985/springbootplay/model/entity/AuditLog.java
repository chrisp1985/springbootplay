package com.chrisp1985.springbootplay.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYERNO")
    private Long id;

    @Column(name = "NAME")
    private String playerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACTION")
    private AuditAction action;

    private Double rating;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    protected AuditLog() {}

    public AuditLog(Long id, String playerName, AuditAction action, Double rating, Instant createdAt) {
        this.id = id;
        this.playerName = playerName;
        this.action = action;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getPlayerName() { return playerName; }
    public AuditAction getAction() { return action; }
    public Double getRating() { return rating; }
    public Instant getCreatedAt() { return createdAt; }
}
