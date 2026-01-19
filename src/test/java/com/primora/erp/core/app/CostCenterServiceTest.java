package com.primora.erp.core.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.core.domain.CostCenter;
import com.primora.erp.core.infra.CostCenterJpaRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CostCenterServiceTest {

    @Mock
    private CostCenterJpaRepository costCenterRepository;

    @InjectMocks
    private CostCenterService costCenterService;

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
    void createCostCenter_ShouldSaveAndReturnCostCenter() {
        String code = "CC01";
        String name = "Cost Center 01";
        when(costCenterRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)).thenReturn(false);
        when(costCenterRepository.save(any(CostCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CostCenter result = costCenterService.createCostCenter(code, name);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCompanyId()).isEqualTo(companyId);
        verify(costCenterRepository).save(any(CostCenter.class));
    }

    @Test
    void createCostCenter_WhenCodeExists_ShouldThrowConflict() {
        String code = "CC01";
        when(costCenterRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)).thenReturn(true);

        assertThatThrownBy(() -> costCenterService.createCostCenter(code, "Name"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
