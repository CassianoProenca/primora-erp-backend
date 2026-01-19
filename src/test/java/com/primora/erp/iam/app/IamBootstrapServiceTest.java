package com.primora.erp.iam.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.iam.domain.Permission;
import com.primora.erp.iam.domain.Role;
import com.primora.erp.iam.infra.PermissionJpaRepository;
import com.primora.erp.iam.infra.RoleJpaRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IamBootstrapServiceTest {

    @Mock
    private PermissionJpaRepository permissionRepository;

    @Mock
    private RoleJpaRepository roleRepository;

    @InjectMocks
    private IamBootstrapService iamBootstrapService;

    @Test
    void bootstrapAdminPermissions_ShouldCreatePermissionsAndAssignToAdmin() {
        Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Administrator", true, Instant.now());
        adminRole.setPermissions(new HashSet<>());
        
        when(permissionRepository.findByCodeIgnoreCase(any())).thenReturn(Optional.empty());
        when(roleRepository.findByCodeIgnoreCaseWithPermissions("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);

        // Mock permission retrieval after creation
        Permission p1 = new Permission(UUID.randomUUID(), "IAM_MANAGE_ROLES", "desc", Instant.now());
        Permission p2 = new Permission(UUID.randomUUID(), "IAM_ASSIGN_ROLES", "desc", Instant.now());
        
        // This is a bit tricky with strict mocking, but let's try to match the flow
        // First call checks if exists (returns empty), then saves.
        // Then it retrieves to assign.
        
        // Let's refine the mock to handle the flow
        when(permissionRepository.findByCodeIgnoreCase("IAM_MANAGE_ROLES"))
                .thenReturn(Optional.empty()) // for check
                .thenReturn(Optional.of(p1)); // for assignment
        
        when(permissionRepository.findByCodeIgnoreCase("IAM_ASSIGN_ROLES"))
                .thenReturn(Optional.empty()) // for check
                .thenReturn(Optional.of(p2)); // for assignment

        IamBootstrapService.BootstrapResult result = iamBootstrapService.bootstrapAdminPermissions();

        assertThat(result.createdPermissions()).isEqualTo(2);
        assertThat(result.addedRoleLinks()).isEqualTo(2);
        verify(permissionRepository, times(2)).save(any(Permission.class));
        verify(roleRepository).save(adminRole);
    }
    
    @Test
    void bootstrapAdminPermissions_WhenPermissionsExist_ShouldOnlyAssign() {
        Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Administrator", true, Instant.now());
        adminRole.setPermissions(new HashSet<>());
        
        Permission p1 = new Permission(UUID.randomUUID(), "IAM_MANAGE_ROLES", "desc", Instant.now());
        Permission p2 = new Permission(UUID.randomUUID(), "IAM_ASSIGN_ROLES", "desc", Instant.now());

        when(permissionRepository.findByCodeIgnoreCase("IAM_MANAGE_ROLES")).thenReturn(Optional.of(p1));
        when(permissionRepository.findByCodeIgnoreCase("IAM_ASSIGN_ROLES")).thenReturn(Optional.of(p2));
        
        when(roleRepository.findByCodeIgnoreCaseWithPermissions("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);

        IamBootstrapService.BootstrapResult result = iamBootstrapService.bootstrapAdminPermissions();

        assertThat(result.createdPermissions()).isEqualTo(0);
        assertThat(result.addedRoleLinks()).isEqualTo(2);
        verify(permissionRepository, times(0)).save(any(Permission.class));
        verify(roleRepository).save(adminRole);
    }
}
