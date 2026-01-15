package com.primora.erp.contratos.app;

import com.primora.erp.contratos.domain.Contract;
import com.primora.erp.contratos.domain.ContractDocument;
import com.primora.erp.contratos.infra.ContractDocumentJpaRepository;
import com.primora.erp.contratos.infra.ContractJpaRepository;
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
public class ContractDocumentService {

    private final ContractDocumentJpaRepository documentRepository;
    private final ContractJpaRepository contractRepository;

    public ContractDocumentService(ContractDocumentJpaRepository documentRepository,
                                   ContractJpaRepository contractRepository) {
        this.documentRepository = documentRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional
    public ContractDocument addDocument(UUID contractId, String fileName, String contentType, String storageUrl) {
        UUID companyId = requireCompanyId();
        Contract contract = contractRepository.findByCompanyIdAndId(companyId, contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));
        ContractDocument document = new ContractDocument(
                UUID.randomUUID(),
                companyId,
                contract.getId(),
                fileName,
                contentType,
                storageUrl,
                Instant.now()
        );
        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Page<ContractDocument> listDocuments(UUID contractId, Pageable pageable) {
        UUID companyId = requireCompanyId();
        boolean exists = contractRepository.findByCompanyIdAndId(companyId, contractId).isPresent();
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found");
        }
        return documentRepository.findByCompanyIdAndContractId(companyId, contractId, pageable);
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
