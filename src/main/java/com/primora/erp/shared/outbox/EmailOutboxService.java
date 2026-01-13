package com.primora.erp.shared.outbox;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailOutboxService {

    private static final String PASSWORD_RESET_TEMPLATE = "PASSWORD_RESET";
    private static final String ONBOARDING_LINK_TEMPLATE = "ONBOARDING_LINK";

    private final EmailOutboxRepository repository;

    public EmailOutboxService(EmailOutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void queuePasswordResetEmail(UUID companyId, String toEmail, String token, UUID createdByUserId) {
        String payload = "{\"token\":\"" + token + "\"}";
        EmailOutbox entry = new EmailOutbox(
                UUID.randomUUID(),
                companyId,
                toEmail,
                PASSWORD_RESET_TEMPLATE,
                payload,
                EmailStatus.QUEUED,
                0,
                null,
                createdByUserId,
                Instant.now(),
                null
        );
        repository.save(entry);
    }

    @Transactional
    public void queueOnboardingEmail(UUID companyId, String toEmail, String token, UUID createdByUserId) {
        String payload = "{\"token\":\"" + token + "\"}";
        EmailOutbox entry = new EmailOutbox(
                UUID.randomUUID(),
                companyId,
                toEmail,
                ONBOARDING_LINK_TEMPLATE,
                payload,
                EmailStatus.QUEUED,
                0,
                null,
                createdByUserId,
                Instant.now(),
                null
        );
        repository.save(entry);
    }
}
