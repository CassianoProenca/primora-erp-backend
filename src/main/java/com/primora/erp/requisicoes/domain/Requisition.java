package com.primora.erp.requisicoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requisitions")
public class Requisition {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "requester_department_id", nullable = false)
    private UUID requesterDepartmentId;

    @Column(name = "target_department_id", nullable = false)
    private UUID targetDepartmentId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "recipient_user_id")
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequisitionStatus status;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Requisition() {
    }

    public Requisition(UUID id, UUID companyId, String title, String description,
                       UUID requesterDepartmentId, UUID targetDepartmentId,
                       UUID authorUserId, UUID recipientUserId,
                       RequisitionStatus status, boolean deleted,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.requesterDepartmentId = requesterDepartmentId;
        this.targetDepartmentId = targetDepartmentId;
        this.authorUserId = authorUserId;
        this.recipientUserId = recipientUserId;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getRequesterDepartmentId() {
        return requesterDepartmentId;
    }

    public UUID getTargetDepartmentId() {
        return targetDepartmentId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public RequisitionStatus getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markRead(Instant now) {
        this.status = RequisitionStatus.READ;
        this.updatedAt = now;
    }

    public void markResolved(Instant now) {
        this.status = RequisitionStatus.RESOLVED;
        this.updatedAt = now;
    }

    public void markDeleted(Instant now) {
        this.deleted = true;
        this.updatedAt = now;
    }
}
