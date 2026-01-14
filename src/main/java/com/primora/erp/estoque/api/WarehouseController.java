package com.primora.erp.estoque.api;

import com.primora.erp.estoque.api.dto.WarehouseRequest;
import com.primora.erp.estoque.api.dto.WarehouseResponse;
import com.primora.erp.estoque.app.WarehouseService;
import com.primora.erp.estoque.domain.Warehouse;
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
@RequestMapping("/estoque/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final AuditService auditService;

    public WarehouseController(WarehouseService warehouseService, AuditService auditService) {
        this.warehouseService = warehouseService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        JwtUser user = currentUser();
        Warehouse warehouse = warehouseService.createWarehouse(request.code(), request.name(), request.active());
        auditService.log(
                "WAREHOUSE_CREATED",
                user.userId(),
                user.companyId(),
                "{\"warehouseId\":\"" + warehouse.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(warehouse));
    }

    @GetMapping
    public ResponseEntity<Page<WarehouseResponse>> list(Pageable pageable) {
        Page<WarehouseResponse> warehouses = warehouseService.listWarehouses(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(warehouses);
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> update(@PathVariable UUID warehouseId,
                                                    @Valid @RequestBody WarehouseRequest request) {
        JwtUser user = currentUser();
        Warehouse warehouse = warehouseService.updateWarehouse(
                warehouseId,
                request.code(),
                request.name(),
                request.active()
        );
        auditService.log(
                "WAREHOUSE_UPDATED",
                user.userId(),
                user.companyId(),
                "{\"warehouseId\":\"" + warehouse.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(warehouse));
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.isActive()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
