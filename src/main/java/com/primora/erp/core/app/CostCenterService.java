package com.primora.erp.core.app;

import com.primora.erp.core.domain.CostCenter;
import com.primora.erp.core.domain.RecordStatus;
import com.primora.erp.core.infra.CostCenterJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CostCenterService {

    private final CostCenterJpaRepository costCenterRepository;

    public CostCenterService(CostCenterJpaRepository costCenterRepository) {
        this.costCenterRepository = costCenterRepository;
    }

    @Transactional
    public CostCenter createCostCenter(String code, String name) {
        UUID companyId = requireCompanyId();
        if (costCenterRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cost center code already exists");
        }

        Instant now = Instant.now();
        CostCenter costCenter = new CostCenter(
                UUID.randomUUID(),
                companyId,
                code,
                name,
                RecordStatus.ACTIVE,
                now,
                now
        );
        return costCenterRepository.save(costCenter);
    }

    @Transactional(readOnly = true)
    public Page<CostCenter> listCostCenters(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return costCenterRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional
    public CostCenter updateCostCenter(UUID costCenterId, String code, String name) {
        UUID companyId = requireCompanyId();
        CostCenter costCenter = costCenterRepository.findByCompanyIdAndId(companyId, costCenterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost center not found"));

        if (costCenterRepository.existsByCompanyIdAndCodeIgnoreCaseAndIdNot(companyId, code, costCenterId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cost center code already exists");
        }

        costCenter.updateDetails(code, name, Instant.now());
        return costCenterRepository.save(costCenter);
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
