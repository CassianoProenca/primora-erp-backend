package com.primora.erp.rh.infra;

import com.primora.erp.rh.domain.Employee;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeJpaRepository extends JpaRepository<Employee, UUID> {

    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Employee> findByCompanyIdAndId(UUID companyId, UUID id);
}
