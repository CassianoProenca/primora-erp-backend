package com.primora.erp.core.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.core.domain.Department;
import com.primora.erp.core.infra.DepartmentJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentJpaRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

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
    void createDepartment_ShouldSaveAndReturnDepartment() {
        String code = "DEPT01";
        String name = "Department 01";
        when(departmentRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department result = departmentService.createDepartment(code, name);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCompanyId()).isEqualTo(companyId);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_WhenCodeExists_ShouldThrowConflict() {
        String code = "DEPT01";
        when(departmentRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(code, "Name"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateDepartment_ShouldUpdateAndReturnDepartment() {
        UUID departmentId = UUID.randomUUID();
        String newCode = "DEPT_NEW";
        String newName = "New Name";
        Department existing = new Department(departmentId, companyId, "OLD", "Old Name", null, null, null);

        when(departmentRepository.findByCompanyIdAndId(companyId, departmentId)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByCompanyIdAndCodeIgnoreCaseAndIdNot(companyId, newCode, departmentId)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department result = departmentService.updateDepartment(departmentId, newCode, newName);

        assertThat(result.getCode()).isEqualTo(newCode);
        assertThat(result.getName()).isEqualTo(newName);
        verify(departmentRepository).save(existing);
    }

    @Test
    void updateDepartment_WhenNotFound_ShouldThrowNotFound() {
        UUID departmentId = UUID.randomUUID();
        when(departmentRepository.findByCompanyIdAndId(companyId, departmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(departmentId, "Code", "Name"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
