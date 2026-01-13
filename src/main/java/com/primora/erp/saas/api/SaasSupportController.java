package com.primora.erp.saas.api;

import com.primora.erp.saas.app.SaasSupportService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/saas/support")
public class SaasSupportController {

    private final SaasSupportService supportService;

    public SaasSupportController(SaasSupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping("/companies/{companyId}/onboarding/resend")
    public ResponseEntity<Void> resendOnboarding(@PathVariable UUID companyId) {
        JwtUser user = currentUser();
        supportService.resendOnboarding(companyId, user.userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/companies/{companyId}/password-reset")
    public ResponseEntity<Void> requestAdminPasswordReset(@PathVariable UUID companyId,
                                                          HttpServletRequest request) {
        JwtUser user = currentUser();
        supportService.requestAdminPasswordReset(
                companyId,
                user.userId(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.noContent().build();
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
