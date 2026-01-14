package com.primora.erp.core.api;

import com.primora.erp.core.api.dto.DepartmentRequest;
import com.primora.erp.core.api.dto.DepartmentResponse;
import com.primora.erp.core.app.DepartmentService;
import com.primora.erp.core.domain.Department;
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
@RequestMapping("/core/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        Department department = departmentService.createDepartment(request.code(), request.name());
        return ResponseEntity.ok(toResponse(department));
    }

    @GetMapping
    public ResponseEntity<Page<DepartmentResponse>> list(Pageable pageable) {
        Page<DepartmentResponse> departments = departmentService.listDepartments(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(departments);
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> update(@PathVariable UUID departmentId,
                                                     @Valid @RequestBody DepartmentRequest request) {
        Department department = departmentService.updateDepartment(departmentId, request.code(), request.name());
        return ResponseEntity.ok(toResponse(department));
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getStatus()
        );
    }
}
