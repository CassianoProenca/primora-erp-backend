package com.primora.erp.financeiro.infra;

import com.primora.erp.financeiro.domain.FinancialEntry;
import com.primora.erp.financeiro.domain.FinancialReferenceType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialEntryJpaRepository extends JpaRepository<FinancialEntry, UUID> {

    Page<FinancialEntry> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<FinancialEntry> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<FinancialEntry> findByCompanyIdAndReferenceTypeAndReferenceId(UUID companyId,
                                                                           FinancialReferenceType referenceType,
                                                                           UUID referenceId);
}
