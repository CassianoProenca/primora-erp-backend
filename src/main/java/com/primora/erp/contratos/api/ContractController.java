package com.primora.erp.contratos.api;

import com.primora.erp.contratos.api.dto.ContractRequest;
import com.primora.erp.contratos.api.dto.ContractResponse;
import com.primora.erp.contratos.app.ContractService;
import com.primora.erp.contratos.domain.Contract;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/contratos")
public class ContractController {

    private final ContractService contractService;
    private final AuditService auditService;

    public ContractController(ContractService contractService, AuditService auditService) {
        this.contractService = contractService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractRequest request) {
        JwtUser user = currentUser();
        Contract contract = contractService.createContract(
                request.title(),
                request.description(),
                request.vendorName(),
                request.startDate(),
                request.endDate(),
                request.status()
        );
        auditService.log(
                "CONTRACT_CREATED",
                user.userId(),
                user.companyId(),
                "{\"contractId\":\"" + contract.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(contract));
    }

    @GetMapping
    public ResponseEntity<Page<ContractResponse>> list(Pageable pageable) {
        Page<ContractResponse> contracts = contractService.listContracts(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/expiring")
    public ResponseEntity<Page<ContractResponse>> listExpiring(@RequestParam(defaultValue = "30") int days,
                                                               Pageable pageable) {
        Page<ContractResponse> contracts = contractService.listExpiringContracts(days, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(contracts);
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<ContractResponse> update(@PathVariable UUID contractId,
                                                   @Valid @RequestBody ContractRequest request) {
        JwtUser user = currentUser();
        Contract contract = contractService.updateContract(
                contractId,
                request.title(),
                request.description(),
                request.vendorName(),
                request.startDate(),
                request.endDate(),
                request.status()
        );
        auditService.log(
                "CONTRACT_UPDATED",
                user.userId(),
                user.companyId(),
                "{\"contractId\":\"" + contract.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(contract));
    }

    private ContractResponse toResponse(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getTitle(),
                contract.getDescription(),
                contract.getVendorName(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getStatus()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
