package com.primora.erp.iam.app;

import com.primora.erp.iam.domain.Permission;
import com.primora.erp.iam.domain.Role;
import com.primora.erp.iam.infra.PermissionJpaRepository;
import com.primora.erp.iam.infra.RoleJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IamBootstrapService {

    private static final List<String> BASE_PERMISSIONS = List.of(
            "IAM_MANAGE_ROLES",
            "IAM_ASSIGN_ROLES"
    );

    private final PermissionJpaRepository permissionRepository;
    private final RoleJpaRepository roleRepository;

    public IamBootstrapService(PermissionJpaRepository permissionRepository, RoleJpaRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public BootstrapResult bootstrapAdminPermissions() {
        int createdPermissions = 0;
        for (String code : BASE_PERMISSIONS) {
            if (permissionRepository.findByCodeIgnoreCase(code).isEmpty()) {
                Permission permission = new Permission(UUID.randomUUID(), code, baseDescription(code), Instant.now());
                permissionRepository.save(permission);
                createdPermissions++;
            }
        }

        Role adminRole = roleRepository.findByCodeIgnoreCaseWithPermissions("ADMIN")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADMIN role not found"));

        int addedRoleLinks = 0;
        for (String code : BASE_PERMISSIONS) {
            Permission permission = permissionRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Permission not found after bootstrap: " + code));
            if (adminRole.getPermissions().stream()
                    .noneMatch(existing -> existing.getCode().equalsIgnoreCase(code))) {
                adminRole.getPermissions().add(permission);
                addedRoleLinks++;
            }
        }

        roleRepository.save(adminRole);
        return new BootstrapResult(createdPermissions, addedRoleLinks);
    }

    private String baseDescription(String code) {
        String normalized = code.toLowerCase(Locale.ROOT);
        if (normalized.equals("iam_manage_roles")) {
            return "Manage roles and permissions";
        }
        if (normalized.equals("iam_assign_roles")) {
            return "Assign roles to users";
        }
        return code;
    }

    public record BootstrapResult(int createdPermissions, int addedRoleLinks) {
    }
}
