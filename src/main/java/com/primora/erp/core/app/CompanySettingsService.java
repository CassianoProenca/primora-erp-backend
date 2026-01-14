package com.primora.erp.core.app;

import com.primora.erp.core.domain.CompanySettings;
import com.primora.erp.core.infra.CompanySettingsJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanySettingsService {

    public static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
    public static final String DEFAULT_LOCALE = "pt-BR";
    public static final String DEFAULT_CURRENCY = "BRL";

    private final CompanySettingsJpaRepository settingsRepository;

    public CompanySettingsService(CompanySettingsJpaRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional
    public CompanySettings getOrCreateSettings() {
        UUID companyId = requireCompanyId();
        return settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> createDefaults(companyId));
    }

    @Transactional
    public CompanySettings updateSettings(String timezone, String locale, String currency) {
        UUID companyId = requireCompanyId();
        CompanySettings settings = settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> createDefaults(companyId));
        settings.update(timezone, locale, currency, Instant.now());
        return settingsRepository.save(settings);
    }

    private CompanySettings createDefaults(UUID companyId) {
        Instant now = Instant.now();
        CompanySettings settings = new CompanySettings(
                UUID.randomUUID(),
                companyId,
                DEFAULT_TIMEZONE,
                DEFAULT_LOCALE,
                DEFAULT_CURRENCY,
                now,
                now
        );
        return settingsRepository.save(settings);
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
