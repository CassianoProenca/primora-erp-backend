package com.primora.erp.core.api;

import com.primora.erp.core.api.dto.CostCenterRequest;
import com.primora.erp.core.api.dto.CostCenterResponse;
import com.primora.erp.core.app.CostCenterService;
import com.primora.erp.core.domain.CostCenter;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/core/cost-centers")
public class CostCenterController {

    private final CostCenterService costCenterService;
    private final AuditService auditService;

    public CostCenterController(CostCenterService costCenterService, AuditService auditService) {
        this.costCenterService = costCenterService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<CostCenterResponse> create(@Valid @RequestBody CostCenterRequest request) {
        JwtUser user = currentUser();
        CostCenter costCenter = costCenterService.createCostCenter(request.code(), request.name());
        auditService.log(
                "COST_CENTER_CREATED",
                user.userId(),
                user.companyId(),
                "{\"costCenterId\":\"" + costCenter.getId() + "\"}"
        );
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
        JwtUser user = currentUser();
        CostCenter costCenter = costCenterService.updateCostCenter(costCenterId, request.code(), request.name());
        auditService.log(
                "COST_CENTER_UPDATED",
                user.userId(),
                user.companyId(),
                "{\"costCenterId\":\"" + costCenter.getId() + "\"}"
        );
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

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
