package com.primora.erp.financeiro.app;

import com.primora.erp.financeiro.domain.FinancialCategory;
import com.primora.erp.financeiro.infra.FinancialCategoryJpaRepository;
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
public class FinancialCategoryService {

    public static final String STOCK_CONSUMPTION_CODE = "STOCK_CONSUMPTION";

    private final FinancialCategoryJpaRepository categoryRepository;

    public FinancialCategoryService(FinancialCategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public FinancialCategory createCategory(String code, String name, boolean active) {
        UUID companyId = requireCompanyId();
        if (categoryRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category code already exists");
        }

        Instant now = Instant.now();
        FinancialCategory category = new FinancialCategory(
                UUID.randomUUID(),
                companyId,
                code,
                name,
                false,
                active,
                now,
                now
        );
        return categoryRepository.save(category);
    }

    @Transactional
    public FinancialCategory updateCategory(UUID categoryId, String code, String name, boolean active) {
        UUID companyId = requireCompanyId();
        FinancialCategory category = categoryRepository.findByCompanyIdAndId(companyId, categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (categoryRepository.existsByCompanyIdAndCodeIgnoreCaseAndIdNot(companyId, code, categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category code already exists");
        }

        category.update(code, name, active, Instant.now());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<FinancialCategory> listCategories(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return categoryRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional
    public FinancialCategory getOrCreateStockConsumptionCategory(UUID companyId) {
        return categoryRepository.findByCompanyIdAndCodeIgnoreCase(companyId, STOCK_CONSUMPTION_CODE)
                .orElseGet(() -> {
                    Instant now = Instant.now();
                    FinancialCategory category = new FinancialCategory(
                            UUID.randomUUID(),
                            companyId,
                            STOCK_CONSUMPTION_CODE,
                            "Stock consumption",
                            true,
                            true,
                            now,
                            now
                    );
                    return categoryRepository.save(category);
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
