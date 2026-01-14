package com.primora.erp.core.infra;

import com.primora.erp.core.domain.CostCenter;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostCenterJpaRepository extends JpaRepository<CostCenter, UUID> {

    Page<CostCenter> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<CostCenter> findByCompanyIdAndId(UUID companyId, UUID id);

    boolean existsByCompanyIdAndCodeIgnoreCase(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeIgnoreCaseAndIdNot(UUID companyId, String code, UUID id);
}
