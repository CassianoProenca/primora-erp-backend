package com.primora.erp.core.api;

import com.primora.erp.core.api.dto.CompanySettingsRequest;
import com.primora.erp.core.api.dto.CompanySettingsResponse;
import com.primora.erp.core.app.CompanySettingsService;
import com.primora.erp.core.domain.CompanySettings;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/core/company-settings")
public class CompanySettingsController {

    private final CompanySettingsService settingsService;
    private final AuditService auditService;

    public CompanySettingsController(CompanySettingsService settingsService, AuditService auditService) {
        this.settingsService = settingsService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<CompanySettingsResponse> getSettings() {
        CompanySettings settings = settingsService.getOrCreateSettings();
        return ResponseEntity.ok(toResponse(settings));
    }

    @PutMapping
    public ResponseEntity<CompanySettingsResponse> updateSettings(
            @Valid @RequestBody CompanySettingsRequest request) {
        JwtUser user = currentUser();
        CompanySettings settings = settingsService.updateSettings(
                request.timezone(),
                request.locale(),
                request.currency()
        );
        auditService.log(
                "COMPANY_SETTINGS_UPDATED",
                user.userId(),
                user.companyId(),
                "{}"
        );
        return ResponseEntity.ok(toResponse(settings));
    }

    private CompanySettingsResponse toResponse(CompanySettings settings) {
        return new CompanySettingsResponse(
                settings.getId(),
                settings.getTimezone(),
                settings.getLocale(),
                settings.getCurrency()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
