package com.primora.erp.shared.audit;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(String action, UUID actorUserId, UUID companyId, String metadata) {
        String safeMetadata = metadata == null ? "{}" : metadata;
        AuditLog log = new AuditLog(
                UUID.randomUUID(),
                companyId,
                actorUserId,
                action,
                null,
                null,
                safeMetadata,
                Instant.now()
        );
        repository.save(log);
    }
}
