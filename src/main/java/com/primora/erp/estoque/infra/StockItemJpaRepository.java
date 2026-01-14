package com.primora.erp.estoque.infra;

import com.primora.erp.estoque.domain.StockItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemJpaRepository extends JpaRepository<StockItem, UUID> {

    Page<StockItem> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<StockItem> findByCompanyIdAndId(UUID companyId, UUID id);

    boolean existsByCompanyIdAndSkuIgnoreCase(UUID companyId, String sku);

    boolean existsByCompanyIdAndSkuIgnoreCaseAndIdNot(UUID companyId, String sku, UUID id);
}
