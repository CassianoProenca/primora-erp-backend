package com.primora.erp.estoque.infra;

import com.primora.erp.estoque.domain.StockMovement;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementJpaRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByCompanyId(UUID companyId, Pageable pageable);

    Page<StockMovement> findByCompanyIdAndWarehouseId(UUID companyId, UUID warehouseId, Pageable pageable);
}
