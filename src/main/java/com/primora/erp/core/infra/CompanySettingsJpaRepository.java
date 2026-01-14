package com.primora.erp.core.infra;

import com.primora.erp.core.domain.CompanySettings;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySettingsJpaRepository extends JpaRepository<CompanySettings, UUID> {

    Optional<CompanySettings> findByCompanyId(UUID companyId);
}
