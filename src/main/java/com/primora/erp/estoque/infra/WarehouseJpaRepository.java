package com.primora.erp.estoque.infra;

import com.primora.erp.estoque.domain.Warehouse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseJpaRepository extends JpaRepository<Warehouse, UUID> {

    Page<Warehouse> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Warehouse> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Warehouse> findFirstByCompanyIdAndActiveTrueOrderByCreatedAtAsc(UUID companyId);

    boolean existsByCompanyIdAndCodeIgnoreCase(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeIgnoreCaseAndIdNot(UUID companyId, String code, UUID id);
}
