package com.primora.erp.core.app;

import com.primora.erp.core.domain.Department;
import com.primora.erp.core.domain.RecordStatus;
import com.primora.erp.core.infra.DepartmentJpaRepository;
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
public class DepartmentService {

    private final DepartmentJpaRepository departmentRepository;

    public DepartmentService(DepartmentJpaRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Department createDepartment(String code, String name) {
        UUID companyId = requireCompanyId();
        if (departmentRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists");
        }

        Instant now = Instant.now();
        Department department = new Department(
                UUID.randomUUID(),
                companyId,
                code,
                name,
                RecordStatus.ACTIVE,
                now,
                now
        );
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public Page<Department> listDepartments(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return departmentRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional
    public Department updateDepartment(UUID departmentId, String code, String name) {
        UUID companyId = requireCompanyId();
        Department department = departmentRepository.findByCompanyIdAndId(companyId, departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        if (departmentRepository.existsByCompanyIdAndCodeIgnoreCaseAndIdNot(companyId, code, departmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists");
        }

        department.updateDetails(code, name, Instant.now());
        return departmentRepository.save(department);
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
