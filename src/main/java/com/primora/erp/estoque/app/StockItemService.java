package com.primora.erp.estoque.app;

import com.primora.erp.estoque.domain.StockItem;
import com.primora.erp.estoque.infra.StockItemJpaRepository;
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
public class StockItemService {

    private final StockItemJpaRepository itemRepository;

    public StockItemService(StockItemJpaRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public StockItem createItem(String sku, String name, String unit, BigDecimal purchaseUnitCost, boolean active) {
        UUID companyId = requireCompanyId();
        if (itemRepository.existsByCompanyIdAndSkuIgnoreCase(companyId, sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item SKU already exists");
        }

        Instant now = Instant.now();
        StockItem item = new StockItem(
                UUID.randomUUID(),
                companyId,
                sku,
                name,
                unit,
                purchaseUnitCost,
                active,
                now,
                now
        );
        return itemRepository.save(item);
    }

    @Transactional
    public StockItem updateItem(UUID itemId, String sku, String name, String unit,
                                BigDecimal purchaseUnitCost, boolean active) {
        UUID companyId = requireCompanyId();
        StockItem item = itemRepository.findByCompanyIdAndId(companyId, itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (itemRepository.existsByCompanyIdAndSkuIgnoreCaseAndIdNot(companyId, sku, itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item SKU already exists");
        }

        item.update(sku, name, unit, purchaseUnitCost, active, Instant.now());
        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public Page<StockItem> listItems(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return itemRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public StockItem getItem(UUID itemId) {
        UUID companyId = requireCompanyId();
        return itemRepository.findByCompanyIdAndId(companyId, itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
