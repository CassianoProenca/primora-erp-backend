package com.primora.erp.estoque.app;

import com.primora.erp.estoque.domain.StockItem;
import com.primora.erp.estoque.domain.StockLevel;
import com.primora.erp.estoque.domain.StockMovement;
import com.primora.erp.estoque.domain.StockMovementType;
import com.primora.erp.estoque.domain.StockReferenceType;
import com.primora.erp.estoque.infra.StockItemJpaRepository;
import com.primora.erp.estoque.infra.StockLevelJpaRepository;
import com.primora.erp.estoque.infra.StockMovementJpaRepository;
import com.primora.erp.estoque.infra.WarehouseJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockMovementService {

    private final StockMovementJpaRepository movementRepository;
    private final StockLevelJpaRepository levelRepository;
    private final StockItemJpaRepository itemRepository;
    private final WarehouseJpaRepository warehouseRepository;

    public StockMovementService(StockMovementJpaRepository movementRepository,
                                StockLevelJpaRepository levelRepository,
                                StockItemJpaRepository itemRepository,
                                WarehouseJpaRepository warehouseRepository) {
        this.movementRepository = movementRepository;
        this.levelRepository = levelRepository;
        this.itemRepository = itemRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public StockMovement recordEntry(UUID warehouseId, UUID itemId, BigDecimal quantity,
                                     StockReferenceType referenceType, UUID referenceId, UUID actorUserId) {
        UUID companyId = requireCompanyId();
        ensureWarehouse(companyId, warehouseId);
        StockItem item = getItem(companyId, itemId);
        StockLevel level = getOrCreateLevel(companyId, warehouseId, itemId);

        Instant now = Instant.now();
        level.increase(quantity, now);
        levelRepository.save(level);

        StockMovement movement = new StockMovement(
                UUID.randomUUID(),
                companyId,
                warehouseId,
                itemId,
                StockMovementType.IN,
                quantity,
                item.getPurchaseUnitCost(),
                referenceType,
                referenceId,
                actorUserId,
                now
        );
        return movementRepository.save(movement);
    }

    @Transactional
    public StockMovement recordExit(UUID warehouseId, UUID itemId, BigDecimal quantity,
                                    StockReferenceType referenceType, UUID referenceId, UUID actorUserId) {
        UUID companyId = requireCompanyId();
        ensureWarehouse(companyId, warehouseId);
        StockItem item = getItem(companyId, itemId);
        StockLevel level = getOrCreateLevel(companyId, warehouseId, itemId);

        if (level.getQuantity().compareTo(quantity) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for item " + item.getSku());
        }

        Instant now = Instant.now();
        level.decrease(quantity, now);
        levelRepository.save(level);

        StockMovement movement = new StockMovement(
                UUID.randomUUID(),
                companyId,
                warehouseId,
                itemId,
                StockMovementType.OUT,
                quantity,
                item.getPurchaseUnitCost(),
                referenceType,
                referenceId,
                actorUserId,
                now
        );
        return movementRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> listMovements(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return movementRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> listMovementsByWarehouse(UUID warehouseId, Pageable pageable) {
        UUID companyId = requireCompanyId();
        return movementRepository.findByCompanyIdAndWarehouseId(companyId, warehouseId, pageable);
    }

    private StockItem getItem(UUID companyId, UUID itemId) {
        return itemRepository.findByCompanyIdAndId(companyId, itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    private void ensureWarehouse(UUID companyId, UUID warehouseId) {
        boolean exists = warehouseRepository.findByCompanyIdAndId(companyId, warehouseId).isPresent();
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found");
        }
    }

    private StockLevel getOrCreateLevel(UUID companyId, UUID warehouseId, UUID itemId) {
        return levelRepository.findByCompanyIdAndWarehouseIdAndItemId(companyId, warehouseId, itemId)
                .orElseGet(() -> new StockLevel(UUID.randomUUID(), companyId, warehouseId, itemId,
                        BigDecimal.ZERO, Instant.now()));
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
