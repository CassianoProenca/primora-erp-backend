package com.primora.erp.estoque.app;

import com.primora.erp.estoque.domain.StockReferenceType;
import com.primora.erp.financeiro.app.FinancialEntryService;
import com.primora.erp.requisicoes.domain.Requisition;
import com.primora.erp.requisicoes.domain.RequisitionItem;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockRequisitionService {

    private final StockMovementService movementService;
    private final WarehouseService warehouseService;
    private final FinancialEntryService entryService;

    public StockRequisitionService(StockMovementService movementService, WarehouseService warehouseService,
                                   FinancialEntryService entryService) {
        this.movementService = movementService;
        this.warehouseService = warehouseService;
        this.entryService = entryService;
    }

    @Transactional
    public void consumeForRequisition(Requisition requisition, List<RequisitionItem> items, UUID actorUserId) {
        if (items.isEmpty()) {
            return;
        }
        UUID warehouseId = warehouseService.getOrCreateDefault().getId();
        for (RequisitionItem item : items) {
            var movement = movementService.recordExit(
                    warehouseId,
                    item.getItemId(),
                    item.getQuantity(),
                    StockReferenceType.REQUISITION,
                    requisition.getId(),
                    actorUserId
            );
            entryService.recordStockMovementExpense(movement, requisition.getRequesterDepartmentId(), null);
        }
    }
}
