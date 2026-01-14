package com.primora.erp.rh.app;

import com.primora.erp.rh.domain.ContractStatus;
import com.primora.erp.rh.domain.EmploymentContract;
import com.primora.erp.rh.infra.EmployeeJpaRepository;
import com.primora.erp.rh.infra.EmploymentContractJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
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
public class EmploymentContractService {

    private final EmploymentContractJpaRepository contractRepository;
    private final EmployeeJpaRepository employeeRepository;

    public EmploymentContractService(EmploymentContractJpaRepository contractRepository,
                                     EmployeeJpaRepository employeeRepository) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmploymentContract createContract(UUID employeeId, String title, LocalDate startDate,
                                             LocalDate endDate, BigDecimal monthlySalary,
                                             ContractStatus status) {
        UUID companyId = requireCompanyId();
        ensureEmployee(companyId, employeeId);
        Instant now = Instant.now();
        EmploymentContract contract = new EmploymentContract(
                UUID.randomUUID(),
                companyId,
                employeeId,
                title,
                startDate,
                endDate,
                monthlySalary,
                status,
                now,
                now
        );
        return contractRepository.save(contract);
    }

    @Transactional
    public EmploymentContract updateContract(UUID contractId, String title, LocalDate startDate,
                                             LocalDate endDate, BigDecimal monthlySalary,
                                             ContractStatus status) {
        UUID companyId = requireCompanyId();
        EmploymentContract contract = contractRepository.findByCompanyIdAndId(companyId, contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));

        contract.update(title, startDate, endDate, monthlySalary, status, Instant.now());
        return contractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public Page<EmploymentContract> listContracts(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return contractRepository.findByCompanyId(companyId, pageable);
    }

    private void ensureEmployee(UUID companyId, UUID employeeId) {
        boolean exists = employeeRepository.findByCompanyIdAndId(companyId, employeeId).isPresent();
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
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
