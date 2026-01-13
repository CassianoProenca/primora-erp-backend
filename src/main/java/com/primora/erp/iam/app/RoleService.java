package com.primora.erp.iam.app;

import com.primora.erp.iam.domain.Permission;
import com.primora.erp.iam.domain.Role;
import com.primora.erp.iam.domain.UserCompanyRole;
import com.primora.erp.iam.infra.PermissionJpaRepository;
import com.primora.erp.iam.infra.RoleJpaRepository;
import com.primora.erp.iam.infra.UserCompanyRoleJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleService {

    private final RoleJpaRepository roleRepository;
    private final PermissionJpaRepository permissionRepository;
    private final UserCompanyRoleJpaRepository userCompanyRoleRepository;

    public RoleService(RoleJpaRepository roleRepository, PermissionJpaRepository permissionRepository,
                       UserCompanyRoleJpaRepository userCompanyRoleRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userCompanyRoleRepository = userCompanyRoleRepository;
    }

    @Transactional
    public Role createRole(String code, String name, Set<String> permissionCodes) {
        if (roleRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role code already exists");
        }

        Role role = new Role(UUID.randomUUID(), code, name, false, Instant.now());
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionCode : permissionCodes) {
                Permission permission = permissionRepository.findByCodeIgnoreCase(permissionCode)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Permission not found: " + permissionCode));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public Page<Role> listRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Transactional
    public void assignRole(UUID userId, UUID roleId) {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (userCompanyRoleRepository.existsByUserIdAndCompanyIdAndRoleId(userId, companyId, roleId)) {
            return;
        }

        UserCompanyRole userCompanyRole = new UserCompanyRole(
                UUID.randomUUID(),
                userId,
                companyId,
                role,
                Instant.now()
        );
        userCompanyRoleRepository.save(userCompanyRole);
    }

    @Transactional(readOnly = true)
    public Page<Role> listRolesForUser(UUID userId, Pageable pageable) {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }

        return userCompanyRoleRepository.findByUserIdAndCompanyId(userId, companyId, pageable)
                .map(UserCompanyRole::getRole);
    }
}
