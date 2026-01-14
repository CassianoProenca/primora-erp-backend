package com.primora.erp.rh.infra;

import com.primora.erp.rh.domain.EmploymentContract;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentContractJpaRepository extends JpaRepository<EmploymentContract, UUID> {

    Page<EmploymentContract> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<EmploymentContract> findByCompanyIdAndId(UUID companyId, UUID id);
}
