package com.primora.erp.core.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.core.domain.CompanySettings;
import com.primora.erp.core.infra.CompanySettingsJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanySettingsServiceTest {

    @Mock
    private CompanySettingsJpaRepository settingsRepository;

    @InjectMocks
    private CompanySettingsService settingsService;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getOrCreateSettings_WhenExists_ShouldReturnExisting() {
        CompanySettings existing = new CompanySettings(UUID.randomUUID(), companyId, "TZ", "LOC", "CUR", null, null);
        when(settingsRepository.findByCompanyId(companyId)).thenReturn(Optional.of(existing));

        CompanySettings result = settingsService.getOrCreateSettings();

        assertThat(result).isSameAs(existing);
        verify(settingsRepository, times(0)).save(any());
    }

    @Test
    void getOrCreateSettings_WhenNotExists_ShouldCreateDefaults() {
        when(settingsRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(CompanySettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanySettings result = settingsService.getOrCreateSettings();

        assertThat(result).isNotNull();
        assertThat(result.getCompanyId()).isEqualTo(companyId);
        assertThat(result.getTimezone()).isEqualTo(CompanySettingsService.DEFAULT_TIMEZONE);
        verify(settingsRepository).save(any(CompanySettings.class));
    }
}
