package com.primora.erp.rh.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.core.infra.DepartmentJpaRepository;
import com.primora.erp.rh.domain.Employee;
import com.primora.erp.rh.domain.EmployeeStatus;
import com.primora.erp.rh.infra.EmployeeJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeJpaRepository employeeRepository;
    @Mock
    private DepartmentJpaRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createEmployee_ShouldSave() {
        String name = "John Doe";
        String email = "john@example.com";
        EmployeeStatus status = EmployeeStatus.ACTIVE;

        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee result = employeeService.createEmployee(name, email, "123", null, status);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getStatus()).isEqualTo(status);
        verify(employeeRepository).save(any(Employee.class));
    }
}
