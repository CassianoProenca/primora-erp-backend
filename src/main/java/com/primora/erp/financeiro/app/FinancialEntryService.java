package com.primora.erp.financeiro.app;

import com.primora.erp.core.app.CompanySettingsService;
import com.primora.erp.core.domain.CompanySettings;
import com.primora.erp.core.infra.CostCenterJpaRepository;
import com.primora.erp.core.infra.DepartmentJpaRepository;
import com.primora.erp.estoque.domain.StockMovement;
import com.primora.erp.financeiro.domain.FinancialCategory;
import com.primora.erp.financeiro.domain.FinancialEntry;
import com.primora.erp.financeiro.domain.FinancialEntryType;
import com.primora.erp.financeiro.domain.FinancialReferenceType;
import com.primora.erp.financeiro.infra.FinancialCategoryJpaRepository;
import com.primora.erp.financeiro.infra.FinancialEntryJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinancialEntryService {

    private final FinancialEntryJpaRepository entryRepository;
    private final FinancialCategoryService categoryService;
    private final CompanySettingsService settingsService;
    private final FinancialCategoryJpaRepository categoryRepository;
    private final DepartmentJpaRepository departmentRepository;
    private final CostCenterJpaRepository costCenterRepository;

    public FinancialEntryService(FinancialEntryJpaRepository entryRepository,
                                 FinancialCategoryService categoryService,
                                 CompanySettingsService settingsService,
                                 FinancialCategoryJpaRepository categoryRepository,
                                 DepartmentJpaRepository departmentRepository,
                                 CostCenterJpaRepository costCenterRepository) {
        this.entryRepository = entryRepository;
        this.categoryService = categoryService;
        this.settingsService = settingsService;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.costCenterRepository = costCenterRepository;
    }

    @Transactional
    public FinancialEntry createManualEntry(FinancialEntryType type, String description, BigDecimal amount,
                                            String currency, LocalDate entryDate, UUID categoryId,
                                            UUID departmentId, UUID costCenterId) {
        UUID companyId = requireCompanyId();
        validateReferences(companyId, categoryId, departmentId, costCenterId);
        String resolvedCurrency = currency == null || currency.isBlank() ? defaultCurrency() : currency;
        Instant now = Instant.now();
        FinancialEntry entry = new FinancialEntry(
                UUID.randomUUID(),
                companyId,
                type,
                description,
                amount,
                resolvedCurrency,
                entryDate,
                categoryId,
                departmentId,
                costCenterId,
                FinancialReferenceType.MANUAL,
                null,
                now,
                now
        );
        return entryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<FinancialEntry> listEntries(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return entryRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional
    public FinancialEntry recordStockMovementExpense(StockMovement movement,
                                                     UUID departmentId,
                                                     UUID costCenterId) {
        UUID companyId = movement.getCompanyId();
        validateReferences(companyId, null, departmentId, costCenterId);
        return entryRepository.findByCompanyIdAndReferenceTypeAndReferenceId(
                        companyId,
                        FinancialReferenceType.STOCK_MOVEMENT,
                        movement.getId()
                )
                .orElseGet(() -> createStockExpenseEntry(movement, departmentId, costCenterId));
    }

    private FinancialEntry createStockExpenseEntry(StockMovement movement, UUID departmentId, UUID costCenterId) {
        FinancialCategory category = categoryService.getOrCreateStockConsumptionCategory(movement.getCompanyId());
        BigDecimal amount = movement.getQuantity()
                .multiply(movement.getUnitCost())
                .setScale(2, RoundingMode.HALF_UP);
        Instant now = Instant.now();
        FinancialEntry entry = new FinancialEntry(
                UUID.randomUUID(),
                movement.getCompanyId(),
                FinancialEntryType.EXPENSE,
                "Stock consumption (movement " + movement.getId() + ")",
                amount,
                defaultCurrency(),
                LocalDate.now(),
                category.getId(),
                departmentId,
                costCenterId,
                FinancialReferenceType.STOCK_MOVEMENT,
                movement.getId(),
                now,
                now
        );
        return entryRepository.save(entry);
    }

    private String defaultCurrency() {
        CompanySettings settings = settingsService.getOrCreateSettings();
        return settings.getCurrency();
    }

    private void validateReferences(UUID companyId, UUID categoryId, UUID departmentId, UUID costCenterId) {
        if (categoryId != null) {
            boolean exists = categoryRepository.findByCompanyIdAndId(companyId, categoryId).isPresent();
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
            }
        }
        if (departmentId != null) {
            boolean exists = departmentRepository.findByCompanyIdAndId(companyId, departmentId).isPresent();
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
            }
        }
        if (costCenterId != null) {
            boolean exists = costCenterRepository.findByCompanyIdAndId(companyId, costCenterId).isPresent();
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost center not found");
            }
        }
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
