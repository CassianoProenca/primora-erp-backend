package com.primora.erp.core.infra;

import com.primora.erp.core.domain.Department;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentJpaRepository extends JpaRepository<Department, UUID> {

    Page<Department> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Department> findByCompanyIdAndId(UUID companyId, UUID id);

    boolean existsByCompanyIdAndCodeIgnoreCase(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeIgnoreCaseAndIdNot(UUID companyId, String code, UUID id);
}
