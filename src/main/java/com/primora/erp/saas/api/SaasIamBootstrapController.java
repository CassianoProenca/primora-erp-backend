package com.primora.erp.saas.api;

import com.primora.erp.auth.domain.UserType;
import com.primora.erp.iam.app.IamBootstrapService;
import com.primora.erp.saas.api.dto.IamBootstrapResponse;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/saas/iam")
public class SaasIamBootstrapController {

    private final IamBootstrapService bootstrapService;
    private final AuditService auditService;

    public SaasIamBootstrapController(IamBootstrapService bootstrapService, AuditService auditService) {
        this.bootstrapService = bootstrapService;
        this.auditService = auditService;
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<IamBootstrapResponse> bootstrap() {
        Optional<JwtUser> jwtUser = CurrentUser.get();
        if (jwtUser.isEmpty() || jwtUser.get().userType() != UserType.SAAS_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only SAAS_OWNER can bootstrap IAM");
        }

        IamBootstrapService.BootstrapResult result = bootstrapService.bootstrapAdminPermissions();
        auditService.log(
                "SAAS_IAM_BOOTSTRAP",
                jwtUser.get().userId(),
                null,
                "{\"createdPermissions\":" + result.createdPermissions()
                        + ",\"addedRoleLinks\":" + result.addedRoleLinks() + "}"
        );
        return ResponseEntity.ok(new IamBootstrapResponse(result.createdPermissions(), result.addedRoleLinks()));
    }
}
