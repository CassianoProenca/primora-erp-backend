package com.primora.erp.rh.api;

import com.primora.erp.rh.api.dto.BenefitRequest;
import com.primora.erp.rh.api.dto.BenefitResponse;
import com.primora.erp.rh.app.BenefitService;
import com.primora.erp.rh.domain.Benefit;
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
@RequestMapping("/rh/benefits")
public class BenefitController {

    private final BenefitService benefitService;

    public BenefitController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    @PostMapping
    public ResponseEntity<BenefitResponse> create(@Valid @RequestBody BenefitRequest request) {
        Benefit benefit = benefitService.createBenefit(
                request.employeeId(),
                request.name(),
                request.amount(),
                request.active()
        );
        return ResponseEntity.ok(toResponse(benefit));
    }

    @GetMapping
    public ResponseEntity<Page<BenefitResponse>> list(Pageable pageable) {
        Page<BenefitResponse> benefits = benefitService.listBenefits(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(benefits);
    }

    @PutMapping("/{benefitId}")
    public ResponseEntity<BenefitResponse> update(@PathVariable UUID benefitId,
                                                  @Valid @RequestBody BenefitRequest request) {
        Benefit benefit = benefitService.updateBenefit(
                benefitId,
                request.name(),
                request.amount(),
                request.active()
        );
        return ResponseEntity.ok(toResponse(benefit));
    }

    private BenefitResponse toResponse(Benefit benefit) {
        return new BenefitResponse(
                benefit.getId(),
                benefit.getEmployeeId(),
                benefit.getName(),
                benefit.getAmount(),
                benefit.isActive()
        );
    }
}
