package com.primora.erp.rh.api;

import com.primora.erp.rh.api.dto.EmploymentContractRequest;
import com.primora.erp.rh.api.dto.EmploymentContractResponse;
import com.primora.erp.rh.app.EmploymentContractService;
import com.primora.erp.rh.domain.EmploymentContract;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rh/contracts")
public class EmploymentContractController {

    private final EmploymentContractService contractService;

    public EmploymentContractController(EmploymentContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ResponseEntity<EmploymentContractResponse> create(@Valid @RequestBody EmploymentContractRequest request) {
        EmploymentContract contract = contractService.createContract(
                request.employeeId(),
                request.title(),
                request.startDate(),
                request.endDate(),
                request.monthlySalary(),
                request.status()
        );
        return ResponseEntity.ok(toResponse(contract));
    }

    @GetMapping
    public ResponseEntity<Page<EmploymentContractResponse>> list(Pageable pageable) {
        Page<EmploymentContractResponse> contracts = contractService.listContracts(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(contracts);
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<EmploymentContractResponse> update(@PathVariable UUID contractId,
                                                             @Valid @RequestBody EmploymentContractRequest request) {
        EmploymentContract contract = contractService.updateContract(
                contractId,
                request.title(),
                request.startDate(),
                request.endDate(),
                request.monthlySalary(),
                request.status()
        );
        return ResponseEntity.ok(toResponse(contract));
    }

    private EmploymentContractResponse toResponse(EmploymentContract contract) {
        return new EmploymentContractResponse(
                contract.getId(),
                contract.getEmployeeId(),
                contract.getTitle(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getMonthlySalary(),
                contract.getStatus()
        );
    }
}
