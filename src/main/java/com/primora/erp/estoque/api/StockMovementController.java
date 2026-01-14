package com.primora.erp.estoque.api;

import com.primora.erp.estoque.api.dto.StockMovementRequest;
import com.primora.erp.estoque.api.dto.StockMovementResponse;
import com.primora.erp.estoque.app.StockMovementService;
import com.primora.erp.estoque.domain.StockMovement;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/estoque/movements")
public class StockMovementController {

    private final StockMovementService movementService;
    private final AuditService auditService;

    public StockMovementController(StockMovementService movementService, AuditService auditService) {
        this.movementService = movementService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponse> create(@Valid @RequestBody StockMovementRequest request) {
        JwtUser user = currentUser();
        StockMovement movement = switch (request.type()) {
            case IN -> movementService.recordEntry(
                    request.warehouseId(),
                    request.itemId(),
                    request.quantity(),
                    request.referenceType(),
                    request.referenceId(),
                    user.userId()
            );
            case OUT -> movementService.recordExit(
                    request.warehouseId(),
                    request.itemId(),
                    request.quantity(),
                    request.referenceType(),
                    request.referenceId(),
                    user.userId()
            );
            case ADJUST -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjust not supported");
        };

        auditService.log(
                "STOCK_MOVEMENT_CREATED",
                user.userId(),
                user.companyId(),
                "{\"movementId\":\"" + movement.getId() + "\"}"
        );

        return ResponseEntity.ok(toResponse(movement));
    }

    @GetMapping
    public ResponseEntity<Page<StockMovementResponse>> list(Pageable pageable,
                                                           @RequestParam(required = false) UUID warehouseId) {
        Page<StockMovementResponse> movements = warehouseId == null
                ? movementService.listMovements(pageable).map(this::toResponse)
                : movementService.listMovementsByWarehouse(warehouseId, pageable).map(this::toResponse);
        return ResponseEntity.ok(movements);
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getWarehouseId(),
                movement.getItemId(),
                movement.getType(),
                movement.getQuantity(),
                movement.getUnitCost(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getCreatedByUserId(),
                movement.getCreatedAt()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
