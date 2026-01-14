package com.primora.erp.estoque.api;

import com.primora.erp.estoque.api.dto.StockItemRequest;
import com.primora.erp.estoque.api.dto.StockItemResponse;
import com.primora.erp.estoque.app.StockItemService;
import com.primora.erp.estoque.domain.StockItem;
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
@RequestMapping("/estoque/items")
public class StockItemController {

    private final StockItemService itemService;
    private final AuditService auditService;

    public StockItemController(StockItemService itemService, AuditService auditService) {
        this.itemService = itemService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<StockItemResponse> create(@Valid @RequestBody StockItemRequest request) {
        JwtUser user = currentUser();
        StockItem item = itemService.createItem(
                request.sku(),
                request.name(),
                request.unit(),
                request.purchaseUnitCost(),
                request.active()
        );
        auditService.log(
                "STOCK_ITEM_CREATED",
                user.userId(),
                user.companyId(),
                "{\"itemId\":\"" + item.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(item));
    }

    @GetMapping
    public ResponseEntity<Page<StockItemResponse>> list(Pageable pageable) {
        Page<StockItemResponse> items = itemService.listItems(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<StockItemResponse> update(@PathVariable UUID itemId,
                                                    @Valid @RequestBody StockItemRequest request) {
        JwtUser user = currentUser();
        StockItem item = itemService.updateItem(
                itemId,
                request.sku(),
                request.name(),
                request.unit(),
                request.purchaseUnitCost(),
                request.active()
        );
        auditService.log(
                "STOCK_ITEM_UPDATED",
                user.userId(),
                user.companyId(),
                "{\"itemId\":\"" + item.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(item));
    }

    private StockItemResponse toResponse(StockItem item) {
        return new StockItemResponse(
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getUnit(),
                item.getPurchaseUnitCost(),
                item.isActive()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
