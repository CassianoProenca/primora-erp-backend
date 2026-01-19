package com.primora.erp.financeiro.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.core.app.CompanySettingsService;
import com.primora.erp.core.domain.CompanySettings;
import com.primora.erp.core.domain.CostCenter;
import com.primora.erp.core.domain.Department;
import com.primora.erp.core.infra.CostCenterJpaRepository;
import com.primora.erp.core.infra.DepartmentJpaRepository;
import com.primora.erp.financeiro.domain.FinancialCategory;
import com.primora.erp.financeiro.domain.FinancialEntry;
import com.primora.erp.financeiro.domain.FinancialEntryType;
import com.primora.erp.financeiro.infra.FinancialCategoryJpaRepository;
import com.primora.erp.financeiro.infra.FinancialEntryJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class
FinancialEntryServiceTest {

    @Mock
    private FinancialEntryJpaRepository entryRepository;
    @Mock
    private FinancialCategoryService categoryService;
    @Mock
    private CompanySettingsService settingsService;
    @Mock
    private FinancialCategoryJpaRepository categoryRepository;
    @Mock
    private DepartmentJpaRepository departmentRepository;
    @Mock
    private CostCenterJpaRepository costCenterRepository;

    @InjectMocks
    private FinancialEntryService financialEntryService;

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
    void createManualEntry_ShouldSave() {
        UUID categoryId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID costCenterId = UUID.randomUUID();

        when(categoryRepository.findByCompanyIdAndId(companyId, categoryId)).thenReturn(Optional.of(new FinancialCategory(categoryId, companyId, "C", "Category", false, true, null, null)));
        when(departmentRepository.findByCompanyIdAndId(companyId, departmentId)).thenReturn(Optional.of(new Department(departmentId, companyId, "D", "N", null, null, null)));
        when(costCenterRepository.findByCompanyIdAndId(companyId, costCenterId)).thenReturn(Optional.of(new CostCenter(costCenterId, companyId, "CC", "N", null, null, null)));
        when(settingsService.getOrCreateSettings()).thenReturn(new CompanySettings(UUID.randomUUID(), companyId, "TZ", "L", "USD", null, null));
        when(entryRepository.save(any(FinancialEntry.class))).thenAnswer(i -> i.getArgument(0));

        FinancialEntry result = financialEntryService.createManualEntry(FinancialEntryType.EXPENSE, "Desc", BigDecimal.TEN, null, LocalDate.now(), categoryId, departmentId, costCenterId);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(result.getCurrency()).isEqualTo("USD");
        verify(entryRepository).save(any(FinancialEntry.class));
    }

    @Test
    void createManualEntry_WhenCategoryNotFound_ShouldThrowNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findByCompanyIdAndId(companyId, categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialEntryService.createManualEntry(FinancialEntryType.EXPENSE, "Desc", BigDecimal.TEN, null, LocalDate.now(), categoryId, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
