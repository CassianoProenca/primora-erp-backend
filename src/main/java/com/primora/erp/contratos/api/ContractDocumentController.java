package com.primora.erp.contratos.api;

import com.primora.erp.contratos.api.dto.ContractDocumentRequest;
import com.primora.erp.contratos.api.dto.ContractDocumentResponse;
import com.primora.erp.contratos.app.ContractDocumentService;
import com.primora.erp.contratos.domain.ContractDocument;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.files.DocumentStorageService;
import com.primora.erp.shared.files.StoredDocument;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/contratos/{contractId}/documents")
public class ContractDocumentController {

    private final ContractDocumentService documentService;
    private final AuditService auditService;
    private final DocumentStorageService storageService;

    public ContractDocumentController(ContractDocumentService documentService, AuditService auditService,
                                      DocumentStorageService storageService) {
        this.documentService = documentService;
        this.auditService = auditService;
        this.storageService = storageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractDocumentResponse> add(@PathVariable UUID contractId,
                                                       @RequestPart("file") MultipartFile file,
                                                       @RequestPart(value = "metadata", required = false)
                                                       ContractDocumentRequest request) {
        JwtUser user = currentUser();
        StoredDocument storedDocument = storageService.storeContractDocument(
                contractId,
                file,
                request == null ? null : request.fileName(),
                request == null ? null : request.contentType()
        );
        ContractDocument document = documentService.addDocument(
                contractId,
                storedDocument.fileName(),
                storedDocument.contentType(),
                storedDocument.storagePath()
        );
        auditService.log(
                "CONTRACT_DOCUMENT_ADDED",
                user.userId(),
                user.companyId(),
                "{\"contractId\":\"" + contractId + "\",\"documentId\":\"" + document.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(document));
    }

    @GetMapping
    public ResponseEntity<Page<ContractDocumentResponse>> list(@PathVariable UUID contractId,
                                                               Pageable pageable) {
        Page<ContractDocumentResponse> documents = documentService.listDocuments(contractId, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(documents);
    }

    private ContractDocumentResponse toResponse(ContractDocument document) {
        return new ContractDocumentResponse(
                document.getId(),
                document.getContractId(),
                document.getFileName(),
                document.getContentType(),
                document.getStorageUrl(),
                document.getCreatedAt()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
