package com.primora.erp.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Permission() {
    }

    public Permission(UUID id, String code, String description, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
