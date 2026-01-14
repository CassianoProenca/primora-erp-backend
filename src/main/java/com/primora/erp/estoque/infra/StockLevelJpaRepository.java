package com.primora.erp.estoque.infra;

import com.primora.erp.estoque.domain.StockLevel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLevelJpaRepository extends JpaRepository<StockLevel, UUID> {

    Optional<StockLevel> findByCompanyIdAndWarehouseIdAndItemId(UUID companyId, UUID warehouseId, UUID itemId);

    Page<StockLevel> findByCompanyIdAndWarehouseId(UUID companyId, UUID warehouseId, Pageable pageable);
}
