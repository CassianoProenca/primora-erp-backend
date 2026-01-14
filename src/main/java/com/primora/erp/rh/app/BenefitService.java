package com.primora.erp.rh.app;

import com.primora.erp.rh.domain.Benefit;
import com.primora.erp.rh.infra.BenefitJpaRepository;
import com.primora.erp.rh.infra.EmployeeJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BenefitService {

    private final BenefitJpaRepository benefitRepository;
    private final EmployeeJpaRepository employeeRepository;

    public BenefitService(BenefitJpaRepository benefitRepository, EmployeeJpaRepository employeeRepository) {
        this.benefitRepository = benefitRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Benefit createBenefit(UUID employeeId, String name, BigDecimal amount, boolean active) {
        UUID companyId = requireCompanyId();
        ensureEmployee(companyId, employeeId);
        Instant now = Instant.now();
        Benefit benefit = new Benefit(
                UUID.randomUUID(),
                companyId,
                employeeId,
                name,
                amount,
                active,
                now,
                now
        );
        return benefitRepository.save(benefit);
    }

    @Transactional
    public Benefit updateBenefit(UUID benefitId, String name, BigDecimal amount, boolean active) {
        UUID companyId = requireCompanyId();
        Benefit benefit = benefitRepository.findByCompanyIdAndId(companyId, benefitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Benefit not found"));

        benefit.update(name, amount, active, Instant.now());
        return benefitRepository.save(benefit);
    }

    @Transactional(readOnly = true)
    public Page<Benefit> listBenefits(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return benefitRepository.findByCompanyId(companyId, pageable);
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
