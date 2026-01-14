package com.primora.erp.core.api;

import com.primora.erp.core.api.dto.CostCenterRequest;
import com.primora.erp.core.api.dto.CostCenterResponse;
import com.primora.erp.core.app.CostCenterService;
import com.primora.erp.core.domain.CostCenter;
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
@RequestMapping("/core/cost-centers")
public class CostCenterController {

    private final CostCenterService costCenterService;

    public CostCenterController(CostCenterService costCenterService) {
        this.costCenterService = costCenterService;
    }

    @PostMapping
    public ResponseEntity<CostCenterResponse> create(@Valid @RequestBody CostCenterRequest request) {
        CostCenter costCenter = costCenterService.createCostCenter(request.code(), request.name());
        return ResponseEntity.ok(toResponse(costCenter));
    }

    @GetMapping
    public ResponseEntity<Page<CostCenterResponse>> list(Pageable pageable) {
        Page<CostCenterResponse> costCenters = costCenterService.listCostCenters(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(costCenters);
    }

    @PutMapping("/{costCenterId}")
    public ResponseEntity<CostCenterResponse> update(@PathVariable UUID costCenterId,
                                                     @Valid @RequestBody CostCenterRequest request) {
        CostCenter costCenter = costCenterService.updateCostCenter(costCenterId, request.code(), request.name());
        return ResponseEntity.ok(toResponse(costCenter));
    }

    private CostCenterResponse toResponse(CostCenter costCenter) {
        return new CostCenterResponse(
                costCenter.getId(),
                costCenter.getCode(),
                costCenter.getName(),
                costCenter.getStatus()
        );
    }
}
