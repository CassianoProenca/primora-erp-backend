package com.primora.erp.saas.app;

import com.primora.erp.onboarding.domain.OnboardingStatus;
import com.primora.erp.saas.domain.SubscriptionStatus;
import java.util.UUID;

public record SaasCompanySummary(
        UUID id,
        String legalName,
        String tradeName,
        String document,
        String status,
        OnboardingStatus onboardingStatus,
        UUID primaryAdminUserId,
        SubscriptionStatus subscriptionStatus
) {
}
