package com.primora.erp.onboarding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    private UUID id;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column
    private String document;

    @Column(nullable = false)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false)
    private OnboardingStatus onboardingStatus;

    @Column(name = "onboarding_step")
    private Integer onboardingStep;

    @Column(name = "primary_admin_user_id")
    private UUID primaryAdminUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Company() {
    }

    public Company(UUID id, String legalName, String tradeName, String document, String status,
                   OnboardingStatus onboardingStatus, Integer onboardingStep, UUID primaryAdminUserId,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.document = document;
        this.status = status;
        this.onboardingStatus = onboardingStatus;
        this.onboardingStep = onboardingStep;
        this.primaryAdminUserId = primaryAdminUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getDocument() {
        return document;
    }

    public String getStatus() {
        return status;
    }

    public OnboardingStatus getOnboardingStatus() {
        return onboardingStatus;
    }

    public Integer getOnboardingStep() {
        return onboardingStep;
    }

    public UUID getPrimaryAdminUserId() {
        return primaryAdminUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markOnboardingStarted(Instant now) {
        this.onboardingStatus = OnboardingStatus.IN_PROGRESS;
        this.onboardingStep = 1;
        this.updatedAt = now;
    }

    public void markOnboardingCompleted(Instant now) {
        this.onboardingStatus = OnboardingStatus.COMPLETED;
        this.onboardingStep = null;
        this.updatedAt = now;
    }
}
