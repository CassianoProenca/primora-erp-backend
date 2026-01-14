package com.primora.erp.financeiro.api;

import com.primora.erp.financeiro.api.dto.FinancialEntryRequest;
import com.primora.erp.financeiro.api.dto.FinancialEntryResponse;
import com.primora.erp.financeiro.app.FinancialEntryService;
import com.primora.erp.financeiro.domain.FinancialEntry;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financeiro/entries")
public class FinancialEntryController {

    private final FinancialEntryService entryService;

    public FinancialEntryController(FinancialEntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping
    public ResponseEntity<FinancialEntryResponse> create(@Valid @RequestBody FinancialEntryRequest request) {
        FinancialEntry entry = entryService.createManualEntry(
                request.type(),
                request.description(),
                request.amount(),
                request.currency(),
                request.entryDate(),
                request.categoryId(),
                request.departmentId(),
                request.costCenterId()
        );
        return ResponseEntity.ok(toResponse(entry));
    }

    @GetMapping
    public ResponseEntity<Page<FinancialEntryResponse>> list(Pageable pageable) {
        Page<FinancialEntryResponse> entries = entryService.listEntries(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(entries);
    }

    private FinancialEntryResponse toResponse(FinancialEntry entry) {
        return new FinancialEntryResponse(
                entry.getId(),
                entry.getType(),
                entry.getDescription(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getEntryDate(),
                entry.getCategoryId(),
                entry.getDepartmentId(),
                entry.getCostCenterId(),
                entry.getReferenceType(),
                entry.getReferenceId()
        );
    }
}
