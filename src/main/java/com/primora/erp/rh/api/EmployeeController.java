package com.primora.erp.rh.api;

import com.primora.erp.rh.api.dto.EmployeeRequest;
import com.primora.erp.rh.api.dto.EmployeeResponse;
import com.primora.erp.rh.app.EmployeeService;
import com.primora.erp.rh.domain.Employee;
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
@RequestMapping("/rh/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        Employee employee = employeeService.createEmployee(
                request.name(),
                request.email(),
                request.document(),
                request.departmentId(),
                request.status()
        );
        return ResponseEntity.ok(toResponse(employee));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> list(Pageable pageable) {
        Page<EmployeeResponse> employees = employeeService.listEmployees(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID employeeId,
                                                   @Valid @RequestBody EmployeeRequest request) {
        Employee employee = employeeService.updateEmployee(
                employeeId,
                request.name(),
                request.email(),
                request.document(),
                request.departmentId(),
                request.status()
        );
        return ResponseEntity.ok(toResponse(employee));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDocument(),
                employee.getDepartmentId(),
                employee.getStatus()
        );
    }
}
