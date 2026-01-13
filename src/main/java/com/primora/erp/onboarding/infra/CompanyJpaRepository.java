package com.primora.erp.onboarding.infra;

import com.primora.erp.onboarding.domain.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyJpaRepository extends JpaRepository<Company, UUID> {
    Page<Company> findByStatusIgnoreCase(String status, Pageable pageable);
    Optional<Company> findByPrimaryAdminUserId(UUID primaryAdminUserId);
}
