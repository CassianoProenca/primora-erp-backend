package com.primora.erp.financeiro.infra;

import com.primora.erp.financeiro.domain.FinancialCategory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialCategoryJpaRepository extends JpaRepository<FinancialCategory, UUID> {

    Page<FinancialCategory> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<FinancialCategory> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<FinancialCategory> findByCompanyIdAndCodeIgnoreCase(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeIgnoreCase(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeIgnoreCaseAndIdNot(UUID companyId, String code, UUID id);
}
