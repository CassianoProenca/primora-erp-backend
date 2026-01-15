package com.primora.erp.contratos.infra;

import com.primora.erp.contratos.domain.ContractDocument;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractDocumentJpaRepository extends JpaRepository<ContractDocument, UUID> {

    Page<ContractDocument> findByCompanyIdAndContractId(UUID companyId, UUID contractId, Pageable pageable);
}
