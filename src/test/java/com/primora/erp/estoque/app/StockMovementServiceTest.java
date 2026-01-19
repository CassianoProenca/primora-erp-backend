package com.primora.erp.estoque.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.estoque.domain.StockItem;
import com.primora.erp.estoque.domain.StockLevel;
import com.primora.erp.estoque.domain.StockMovement;
import com.primora.erp.estoque.domain.StockReferenceType;
import com.primora.erp.estoque.domain.Warehouse;
import com.primora.erp.estoque.infra.StockItemJpaRepository;
import com.primora.erp.estoque.infra.StockLevelJpaRepository;
import com.primora.erp.estoque.infra.StockMovementJpaRepository;
import com.primora.erp.estoque.infra.WarehouseJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementJpaRepository movementRepository;
    @Mock
    private StockLevelJpaRepository levelRepository;
    @Mock
    private StockItemJpaRepository itemRepository;
    @Mock
    private WarehouseJpaRepository warehouseRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void recordEntry_ShouldIncreaseStockAndSaveMovement() {
        UUID warehouseId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        BigDecimal quantity = BigDecimal.TEN;
        Warehouse warehouse = new Warehouse(warehouseId, companyId, "W", "Warehouse", true, Instant.now(), Instant.now());
        StockItem item = new StockItem(itemId, companyId, "SKU", "N", "U", BigDecimal.ONE, true, null, null);
        StockLevel level = new StockLevel(UUID.randomUUID(), companyId, warehouseId, itemId, BigDecimal.ZERO, Instant.now());

        when(warehouseRepository.findByCompanyIdAndId(companyId, warehouseId)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findByCompanyIdAndId(companyId, itemId)).thenReturn(Optional.of(item));
        when(levelRepository.findByCompanyIdAndWarehouseIdAndItemId(companyId, warehouseId, itemId)).thenReturn(Optional.of(level));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovement result = stockMovementService.recordEntry(warehouseId, itemId, quantity, StockReferenceType.MANUAL, null, UUID.randomUUID());

        assertThat(level.getQuantity()).isEqualTo(quantity);
        assertThat(result.getQuantity()).isEqualTo(quantity);
        verify(levelRepository).save(level);
        verify(movementRepository).save(any(StockMovement.class));
    }
    
    @Test
    void recordExit_ShouldDecreaseStock() {
        UUID warehouseId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        BigDecimal quantity = BigDecimal.ONE;
        Warehouse warehouse = new Warehouse(warehouseId, companyId, "W", "Warehouse", true, Instant.now(), Instant.now());
        StockItem item = new StockItem(itemId, companyId, "SKU", "N", "U", BigDecimal.ONE, true, null, null);
        StockLevel level = new StockLevel(UUID.randomUUID(), companyId, warehouseId, itemId, BigDecimal.TEN, Instant.now());

        when(warehouseRepository.findByCompanyIdAndId(companyId, warehouseId)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findByCompanyIdAndId(companyId, itemId)).thenReturn(Optional.of(item));
        when(levelRepository.findByCompanyIdAndWarehouseIdAndItemId(companyId, warehouseId, itemId)).thenReturn(Optional.of(level));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.recordExit(warehouseId, itemId, quantity, StockReferenceType.MANUAL, null, UUID.randomUUID());

        assertThat(level.getQuantity()).isEqualTo(new BigDecimal("9"));
    }

    @Test
    void recordExit_WhenInsufficientStock_ShouldThrowConflict() {
        UUID warehouseId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Warehouse warehouse = new Warehouse(warehouseId, companyId, "W", "Warehouse", true, Instant.now(), Instant.now());
        StockItem item = new StockItem(itemId, companyId, "SKU", "N", "U", BigDecimal.ONE, true, null, null);
        StockLevel level = new StockLevel(UUID.randomUUID(), companyId, warehouseId, itemId, BigDecimal.ZERO, Instant.now());

        when(warehouseRepository.findByCompanyIdAndId(companyId, warehouseId)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findByCompanyIdAndId(companyId, itemId)).thenReturn(Optional.of(item));
        when(levelRepository.findByCompanyIdAndWarehouseIdAndItemId(companyId, warehouseId, itemId)).thenReturn(Optional.of(level));

        assertThatThrownBy(() -> stockMovementService.recordExit(warehouseId, itemId, BigDecimal.ONE, StockReferenceType.MANUAL, null, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
