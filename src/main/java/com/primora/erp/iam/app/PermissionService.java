package com.primora.erp.iam.app;

import com.primora.erp.iam.domain.Permission;
import com.primora.erp.iam.infra.PermissionJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermissionService {

    private final PermissionJpaRepository permissionRepository;

    public PermissionService(PermissionJpaRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public Permission createPermission(String code, String description) {
        if (permissionRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Permission code already exists");
        }

        Permission permission = new Permission(UUID.randomUUID(), code, description, Instant.now());
        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public Page<Permission> listPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }
}
