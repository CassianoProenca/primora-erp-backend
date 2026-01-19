package com.primora.erp.iam.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.iam.domain.Permission;
import com.primora.erp.iam.domain.Role;
import com.primora.erp.iam.domain.UserCompanyRole;
import com.primora.erp.iam.infra.PermissionJpaRepository;
import com.primora.erp.iam.infra.RoleJpaRepository;
import com.primora.erp.iam.infra.UserCompanyRoleJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
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
class RoleServiceTest {

    @Mock
    private RoleJpaRepository roleRepository;
    @Mock
    private PermissionJpaRepository permissionRepository;
    @Mock
    private UserCompanyRoleJpaRepository userCompanyRoleRepository;

    @InjectMocks
    private RoleService roleService;
    
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
    void createRole_ShouldSaveRole_WhenCodeIsUnique() {
        String code = "MANAGER";
        String name = "Manager Role";
        when(roleRepository.findByCodeIgnoreCase(code)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        Role role = roleService.createRole(code, name, null);

        assertThat(role).isNotNull();
        assertThat(role.getCode()).isEqualTo(code);
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    void createRole_WithPermissions_ShouldAssignPermissions() {
        String code = "MANAGER";
        String permCode = "EDIT_USER";
        Permission perm = new Permission(UUID.randomUUID(), permCode, "desc", Instant.now());
        
        when(roleRepository.findByCodeIgnoreCase(code)).thenReturn(Optional.empty());
        when(permissionRepository.findByCodeIgnoreCase(permCode)).thenReturn(Optional.of(perm));
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        Role role = roleService.createRole(code, "Manager", Set.of(permCode));

        assertThat(role.getPermissions()).hasSize(1);
        assertThat(role.getPermissions().iterator().next().getCode()).isEqualTo(permCode);
    }

    @Test
    void assignRole_ShouldSaveUserCompanyRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Role role = new Role(roleId, "USER", "User", false, Instant.now());

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userCompanyRoleRepository.existsByUserIdAndCompanyIdAndRoleId(userId, companyId, roleId)).thenReturn(false);

        roleService.assignRole(userId, roleId);

        verify(userCompanyRoleRepository).save(any(UserCompanyRole.class));
    }
    
    @Test
    void assignRole_WhenAlreadyAssigned_ShouldDoNothing() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Role role = new Role(roleId, "USER", "User", false, Instant.now());

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userCompanyRoleRepository.existsByUserIdAndCompanyIdAndRoleId(userId, companyId, roleId)).thenReturn(true);

        roleService.assignRole(userId, roleId);

        verify(userCompanyRoleRepository, org.mockito.Mockito.never()).save(any());
    }
}
