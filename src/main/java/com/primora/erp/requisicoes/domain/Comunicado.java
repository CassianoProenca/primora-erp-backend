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
@Table(name = "comunicados")
public class Comunicado {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

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
    private ComunicadoStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Comunicado() {
    }

    public Comunicado(UUID id, UUID companyId, String title, String message,
                      UUID requesterDepartmentId, UUID targetDepartmentId,
                      UUID authorUserId, UUID recipientUserId, ComunicadoStatus status,
                      Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.title = title;
        this.message = message;
        this.requesterDepartmentId = requesterDepartmentId;
        this.targetDepartmentId = targetDepartmentId;
        this.authorUserId = authorUserId;
        this.recipientUserId = recipientUserId;
        this.status = status;
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

    public String getMessage() {
        return message;
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

    public ComunicadoStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markReceived(Instant now) {
        this.status = ComunicadoStatus.RECEIVED;
        this.updatedAt = now;
    }

    public void markRead(Instant now) {
        this.status = ComunicadoStatus.READ;
        this.updatedAt = now;
    }
}
