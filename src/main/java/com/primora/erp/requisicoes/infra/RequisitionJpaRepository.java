package com.primora.erp.requisicoes.infra;

import com.primora.erp.requisicoes.domain.Requisition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitionJpaRepository extends JpaRepository<Requisition, UUID> {

    Page<Requisition> findByCompanyIdAndDeletedFalse(UUID companyId, Pageable pageable);

    Optional<Requisition> findByCompanyIdAndIdAndDeletedFalse(UUID companyId, UUID id);
}
