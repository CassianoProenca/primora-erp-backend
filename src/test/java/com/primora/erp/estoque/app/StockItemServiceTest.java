package com.primora.erp.estoque.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.estoque.domain.StockItem;
import com.primora.erp.estoque.infra.StockItemJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
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
class StockItemServiceTest {

    @Mock
    private StockItemJpaRepository itemRepository;

    @InjectMocks
    private StockItemService stockItemService;

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
    void createItem_ShouldSave() {
        String sku = "SKU123";
        when(itemRepository.existsByCompanyIdAndSkuIgnoreCase(companyId, sku)).thenReturn(false);
        when(itemRepository.save(any(StockItem.class))).thenAnswer(i -> i.getArgument(0));

        StockItem result = stockItemService.createItem(sku, "Name", "UN", BigDecimal.TEN, true);

        assertThat(result.getSku()).isEqualTo(sku);
        verify(itemRepository).save(any(StockItem.class));
    }

    @Test
    void createItem_WhenSkuExists_ShouldThrowConflict() {
        String sku = "SKU123";
        when(itemRepository.existsByCompanyIdAndSkuIgnoreCase(companyId, sku)).thenReturn(true);

        assertThatThrownBy(() -> stockItemService.createItem(sku, "Name", "UN", BigDecimal.TEN, true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
