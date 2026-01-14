package com.primora.erp.rh.app;

import com.primora.erp.core.infra.DepartmentJpaRepository;
import com.primora.erp.rh.domain.Employee;
import com.primora.erp.rh.domain.EmployeeStatus;
import com.primora.erp.rh.infra.EmployeeJpaRepository;
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
public class EmployeeService {

    private final EmployeeJpaRepository employeeRepository;
    private final DepartmentJpaRepository departmentRepository;

    public EmployeeService(EmployeeJpaRepository employeeRepository, DepartmentJpaRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Employee createEmployee(String name, String email, String document, UUID departmentId,
                                   EmployeeStatus status) {
        UUID companyId = requireCompanyId();
        if (departmentId != null) {
            ensureDepartment(companyId, departmentId);
        }

        Instant now = Instant.now();
        Employee employee = new Employee(
                UUID.randomUUID(),
                companyId,
                name,
                email,
                document,
                departmentId,
                status,
                now,
                now
        );
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(UUID employeeId, String name, String email, String document,
                                   UUID departmentId, EmployeeStatus status) {
        UUID companyId = requireCompanyId();
        Employee employee = employeeRepository.findByCompanyIdAndId(companyId, employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        if (departmentId != null) {
            ensureDepartment(companyId, departmentId);
        }

        employee.update(name, email, document, departmentId, status, Instant.now());
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Page<Employee> listEmployees(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return employeeRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public Employee getEmployee(UUID employeeId) {
        UUID companyId = requireCompanyId();
        return employeeRepository.findByCompanyIdAndId(companyId, employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void ensureDepartment(UUID companyId, UUID departmentId) {
        boolean exists = departmentRepository.findByCompanyIdAndId(companyId, departmentId).isPresent();
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
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
