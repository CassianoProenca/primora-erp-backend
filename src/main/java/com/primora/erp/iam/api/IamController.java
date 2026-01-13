package com.primora.erp.iam.api;

import com.primora.erp.iam.api.dto.AssignRoleRequest;
import com.primora.erp.iam.api.dto.CreatePermissionRequest;
import com.primora.erp.iam.api.dto.CreateRoleRequest;
import com.primora.erp.iam.api.dto.PermissionResponse;
import com.primora.erp.iam.api.dto.RoleResponse;
import com.primora.erp.iam.app.PermissionChecker;
import com.primora.erp.iam.app.PermissionService;
import com.primora.erp.iam.app.RoleService;
import com.primora.erp.iam.domain.Permission;
import com.primora.erp.iam.domain.Role;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import com.primora.erp.shared.security.TenantContext;

@RestController
@RequestMapping("/iam")
public class IamController {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final PermissionChecker permissionChecker;
    private final AuditService auditService;

    public IamController(RoleService roleService, PermissionService permissionService,
                         PermissionChecker permissionChecker, AuditService auditService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.permissionChecker = permissionChecker;
        this.auditService = auditService;
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        permissionChecker.require("IAM_MANAGE_ROLES");
        Role role = roleService.createRole(request.code(), request.name(), request.permissionCodes());
        auditService.log(
                "IAM_ROLE_CREATED",
                currentUserId(),
                currentCompanyId(),
                "{\"roleId\":\"" + role.getId() + "\"}"
        );
        return ResponseEntity.ok(toRoleResponse(role));
    }

    @GetMapping("/roles")
    public ResponseEntity<Page<RoleResponse>> listRoles(Pageable pageable) {
        permissionChecker.require("IAM_MANAGE_ROLES");
        auditService.log(
                "IAM_ROLE_LIST_VIEWED",
                currentUserId(),
                currentCompanyId(),
                "{}"
        );
        Page<RoleResponse> roles = roleService.listRoles(pageable)
                .map(this::toRoleResponse);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/permissions")
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        permissionChecker.require("IAM_MANAGE_ROLES");
        Permission permission = permissionService.createPermission(request.code(), request.description());
        auditService.log(
                "IAM_PERMISSION_CREATED",
                currentUserId(),
                currentCompanyId(),
                "{\"permissionId\":\"" + permission.getId() + "\"}"
        );
        return ResponseEntity.ok(toPermissionResponse(permission));
    }

    @GetMapping("/permissions")
    public ResponseEntity<Page<PermissionResponse>> listPermissions(Pageable pageable) {
        permissionChecker.require("IAM_MANAGE_ROLES");
        auditService.log(
                "IAM_PERMISSION_LIST_VIEWED",
                currentUserId(),
                currentCompanyId(),
                "{}"
        );
        Page<PermissionResponse> permissions = permissionService.listPermissions(pageable)
                .map(this::toPermissionResponse);
        return ResponseEntity.ok(permissions);
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<Void> assignRole(@PathVariable UUID userId, @Valid @RequestBody AssignRoleRequest request) {
        permissionChecker.require("IAM_ASSIGN_ROLES");
        roleService.assignRole(userId, request.roleId());
        auditService.log(
                "IAM_ROLE_ASSIGNED",
                currentUserId(),
                currentCompanyId(),
                "{\"userId\":\"" + userId + "\",\"roleId\":\"" + request.roleId() + "\"}"
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<Page<RoleResponse>> listUserRoles(@PathVariable UUID userId, Pageable pageable) {
        permissionChecker.require("IAM_ASSIGN_ROLES");
        auditService.log(
                "IAM_USER_ROLES_VIEWED",
                currentUserId(),
                currentCompanyId(),
                "{\"userId\":\"" + userId + "\"}"
        );
        Page<RoleResponse> roles = roleService.listRolesForUser(userId, pageable)
                .map(this::toRoleResponse);
        return ResponseEntity.ok(roles);
    }

    private RoleResponse toRoleResponse(Role role) {
        Set<String> permissionCodes = role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.isSystem(), permissionCodes);
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getCode(), permission.getDescription());
    }

    private UUID currentUserId() {
        JwtUser user = CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        return user.userId();
    }

    private UUID currentCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
