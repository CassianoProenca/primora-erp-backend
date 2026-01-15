package com.primora.erp.contratos.app;

import com.primora.erp.contratos.domain.Contract;
import com.primora.erp.contratos.domain.ContractStatus;
import com.primora.erp.contratos.infra.ContractJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContractService {

    private final ContractJpaRepository contractRepository;

    public ContractService(ContractJpaRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Transactional
    public Contract createContract(String title, String description, String vendorName,
                                   LocalDate startDate, LocalDate endDate, ContractStatus status) {
        UUID companyId = requireCompanyId();
        validateDates(startDate, endDate);
        Instant now = Instant.now();
        Contract contract = new Contract(
                UUID.randomUUID(),
                companyId,
                title,
                description,
                vendorName,
                startDate,
                endDate,
                status,
                now,
                now
        );
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract updateContract(UUID contractId, String title, String description, String vendorName,
                                   LocalDate startDate, LocalDate endDate, ContractStatus status) {
        UUID companyId = requireCompanyId();
        validateDates(startDate, endDate);
        Contract contract = contractRepository.findByCompanyIdAndId(companyId, contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));

        contract.update(title, description, vendorName, startDate, endDate, status, Instant.now());
        return contractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public Page<Contract> listContracts(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return contractRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Contract> listExpiringContracts(int daysAhead, Pageable pageable) {
        if (daysAhead <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Days ahead must be greater than zero");
        }
        UUID companyId = requireCompanyId();
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);
        return contractRepository.findExpiringContracts(companyId, ContractStatus.ACTIVE, today, limit, pageable);
    }

    @Transactional(readOnly = true)
    public Contract getContract(UUID contractId) {
        UUID companyId = requireCompanyId();
        return contractRepository.findByCompanyIdAndId(companyId, contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
