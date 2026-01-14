package com.primora.erp.estoque.app;

import com.primora.erp.estoque.domain.StockLevel;
import com.primora.erp.estoque.infra.StockLevelJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockLevelService {

    private final StockLevelJpaRepository levelRepository;

    public StockLevelService(StockLevelJpaRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    @Transactional(readOnly = true)
    public Page<StockLevel> listLevels(UUID warehouseId, Pageable pageable) {
        UUID companyId = requireCompanyId();
        return levelRepository.findByCompanyIdAndWarehouseId(companyId, warehouseId, pageable);
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
