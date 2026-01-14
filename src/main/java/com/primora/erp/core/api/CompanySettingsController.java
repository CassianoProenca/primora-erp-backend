package com.primora.erp.core.api;

import com.primora.erp.core.api.dto.CompanySettingsRequest;
import com.primora.erp.core.api.dto.CompanySettingsResponse;
import com.primora.erp.core.app.CompanySettingsService;
import com.primora.erp.core.domain.CompanySettings;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/company-settings")
public class CompanySettingsController {

    private final CompanySettingsService settingsService;

    public CompanySettingsController(CompanySettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<CompanySettingsResponse> getSettings() {
        CompanySettings settings = settingsService.getOrCreateSettings();
        return ResponseEntity.ok(toResponse(settings));
    }

    @PutMapping
    public ResponseEntity<CompanySettingsResponse> updateSettings(
            @Valid @RequestBody CompanySettingsRequest request) {
        CompanySettings settings = settingsService.updateSettings(
                request.timezone(),
                request.locale(),
                request.currency()
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
}
