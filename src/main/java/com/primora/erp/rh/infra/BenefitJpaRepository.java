package com.primora.erp.rh.infra;

import com.primora.erp.rh.domain.Benefit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitJpaRepository extends JpaRepository<Benefit, UUID> {

    Page<Benefit> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Benefit> findByCompanyIdAndId(UUID companyId, UUID id);
}
