package com.primora.erp.contratos.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.contratos.domain.Contract;
import com.primora.erp.contratos.domain.ContractStatus;
import com.primora.erp.contratos.infra.ContractJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.LocalDate;
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
class ContractServiceTest {

    @Mock
    private ContractJpaRepository contractRepository;

    @InjectMocks
    private ContractService contractService;

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
    void createContract_ShouldSave() {
        String title = "Services Agreement";
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArgument(0));

        Contract result = contractService.createContract(title, "Desc", "Vendor", start, end, ContractStatus.ACTIVE);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void createContract_WhenEndDateBeforeStartDate_ShouldThrowBadRequest() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        assertThatThrownBy(() -> contractService.createContract("T", "D", "V", start, end, ContractStatus.ACTIVE))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
