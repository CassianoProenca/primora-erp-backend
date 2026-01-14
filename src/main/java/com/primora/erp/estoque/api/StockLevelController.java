package com.primora.erp.estoque.api;

import com.primora.erp.estoque.api.dto.StockLevelResponse;
import com.primora.erp.estoque.app.StockLevelService;
import com.primora.erp.estoque.domain.StockLevel;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estoque/warehouses/{warehouseId}/levels")
public class StockLevelController {

    private final StockLevelService levelService;

    public StockLevelController(StockLevelService levelService) {
        this.levelService = levelService;
    }

    @GetMapping
    public ResponseEntity<Page<StockLevelResponse>> list(@PathVariable UUID warehouseId, Pageable pageable) {
        Page<StockLevelResponse> levels = levelService.listLevels(warehouseId, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(levels);
    }

    private StockLevelResponse toResponse(StockLevel level) {
        return new StockLevelResponse(level.getItemId(), level.getQuantity(), level.getUpdatedAt());
    }
}
