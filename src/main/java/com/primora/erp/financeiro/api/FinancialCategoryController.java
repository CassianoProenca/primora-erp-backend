package com.primora.erp.financeiro.api;

import com.primora.erp.financeiro.api.dto.FinancialCategoryRequest;
import com.primora.erp.financeiro.api.dto.FinancialCategoryResponse;
import com.primora.erp.financeiro.app.FinancialCategoryService;
import com.primora.erp.financeiro.domain.FinancialCategory;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financeiro/categories")
public class FinancialCategoryController {

    private final FinancialCategoryService categoryService;

    public FinancialCategoryController(FinancialCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<FinancialCategoryResponse> create(@Valid @RequestBody FinancialCategoryRequest request) {
        FinancialCategory category = categoryService.createCategory(
                request.code(),
                request.name(),
                request.active()
        );
        return ResponseEntity.ok(toResponse(category));
    }

    @GetMapping
    public ResponseEntity<Page<FinancialCategoryResponse>> list(Pageable pageable) {
        Page<FinancialCategoryResponse> categories = categoryService.listCategories(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<FinancialCategoryResponse> update(@PathVariable UUID categoryId,
                                                            @Valid @RequestBody FinancialCategoryRequest request) {
        FinancialCategory category = categoryService.updateCategory(
                categoryId,
                request.code(),
                request.name(),
                request.active()
        );
        return ResponseEntity.ok(toResponse(category));
    }

    private FinancialCategoryResponse toResponse(FinancialCategory category) {
        return new FinancialCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.isSystem(),
                category.isActive()
        );
    }
}
