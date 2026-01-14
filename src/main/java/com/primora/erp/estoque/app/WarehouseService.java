package com.primora.erp.estoque.app;

import com.primora.erp.estoque.domain.Warehouse;
import com.primora.erp.estoque.infra.WarehouseJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WarehouseService {

    private static final String DEFAULT_CODE = "MAIN";
    private static final String DEFAULT_NAME = "Main Warehouse";

    private final WarehouseJpaRepository warehouseRepository;

    public WarehouseService(WarehouseJpaRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public Warehouse createWarehouse(String code, String name, boolean active) {
        UUID companyId = requireCompanyId();
        if (warehouseRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Warehouse code already exists");
        }

        Instant now = Instant.now();
        Warehouse warehouse = new Warehouse(
                UUID.randomUUID(),
                companyId,
                code,
                name,
                active,
                now,
                now
        );
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(UUID warehouseId, String code, String name, boolean active) {
        UUID companyId = requireCompanyId();
        Warehouse warehouse = warehouseRepository.findByCompanyIdAndId(companyId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        if (warehouseRepository.existsByCompanyIdAndCodeIgnoreCaseAndIdNot(companyId, code, warehouseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Warehouse code already exists");
        }

        warehouse.update(code, name, active, Instant.now());
        return warehouseRepository.save(warehouse);
    }

    @Transactional(readOnly = true)
    public Page<Warehouse> listWarehouses(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return warehouseRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional
    public Warehouse getOrCreateDefault() {
        UUID companyId = requireCompanyId();
        return warehouseRepository.findFirstByCompanyIdAndActiveTrueOrderByCreatedAtAsc(companyId)
                .orElseGet(() -> {
                    Instant now = Instant.now();
                    Warehouse warehouse = new Warehouse(
                            UUID.randomUUID(),
                            companyId,
                            DEFAULT_CODE,
                            DEFAULT_NAME,
                            true,
                            now,
                            now
                    );
                    return warehouseRepository.save(warehouse);
                });
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
