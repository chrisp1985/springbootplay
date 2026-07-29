package com.chrisp1985.springbootplay.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name="audit_log")
public record AuditLog(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "PLAYERNO")
        Long id,
        String name,
        String action
) {
}
