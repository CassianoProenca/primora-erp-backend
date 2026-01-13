package com.primora.erp.iam.app;

import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import com.primora.erp.shared.security.TenantContext;
import com.primora.erp.iam.infra.UserCompanyRoleJpaRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermissionChecker {

    private final UserCompanyRoleJpaRepository userCompanyRoleRepository;

    public PermissionChecker(UserCompanyRoleJpaRepository userCompanyRoleRepository) {
        this.userCompanyRoleRepository = userCompanyRoleRepository;
    }

    public void require(String permissionCode) {
        JwtUser user = CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));

        if (CurrentUser.isSaasUser(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission not applicable");
        }

        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }

        boolean allowed = userCompanyRoleRepository.hasPermission(user.userId(), companyId, permissionCode);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied");
        }
    }
}
