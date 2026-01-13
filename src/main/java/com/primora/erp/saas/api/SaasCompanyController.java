package com.primora.erp.saas.api;

import com.primora.erp.saas.api.dto.SaasCompanyResponse;
import com.primora.erp.saas.api.dto.SaasSubscriptionResponse;
import com.primora.erp.saas.app.SaasCompanyService;
import com.primora.erp.saas.app.SaasCompanySummary;
import com.primora.erp.saas.domain.Subscription;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/saas/companies")
public class SaasCompanyController {

    private final SaasCompanyService companyService;
    private final AuditService auditService;

    public SaasCompanyController(SaasCompanyService companyService, AuditService auditService) {
        this.companyService = companyService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<SaasCompanyResponse>> listCompanies(@RequestParam(required = false) String status,
                                                                   Pageable pageable) {
        JwtUser user = currentUser();
        Page<SaasCompanyResponse> response = companyService.listCompanies(status, pageable)
                .map(this::toResponse);

        auditService.log(
                "SAAS_COMPANY_LIST_VIEWED",
                user.userId(),
                null,
                "{\"status\":\"" + (status == null ? "ACTIVE" : status) + "\"}"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}/subscription")
    public ResponseEntity<SaasSubscriptionResponse> getSubscription(@PathVariable UUID companyId) {
        JwtUser user = currentUser();
        Subscription subscription = companyService.getSubscription(companyId);
        auditService.log(
                "SAAS_COMPANY_SUBSCRIPTION_VIEWED",
                user.userId(),
                companyId,
                "{\"companyId\":\"" + companyId + "\"}"
        );
        return ResponseEntity.ok(toResponse(subscription));
    }

    private SaasCompanyResponse toResponse(SaasCompanySummary summary) {
        return new SaasCompanyResponse(
                summary.id(),
                summary.legalName(),
                summary.tradeName(),
                summary.document(),
                summary.status(),
                summary.onboardingStatus(),
                summary.primaryAdminUserId(),
                summary.subscriptionStatus()
        );
    }

    private SaasSubscriptionResponse toResponse(Subscription subscription) {
        return new SaasSubscriptionResponse(
                subscription.getId(),
                subscription.getCompanyId(),
                subscription.getPlanCode(),
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.isAutoRenew(),
                subscription.isCancelAtPeriodEnd(),
                subscription.getProvider(),
                subscription.getProviderCustomerId(),
                subscription.getProviderSubscriptionId()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
