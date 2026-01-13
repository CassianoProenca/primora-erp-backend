package com.primora.erp.saas.api.dto;

import com.primora.erp.saas.domain.SubscriptionStatus;
import java.time.Instant;
import java.util.UUID;

public record SaasSubscriptionResponse(
        UUID id,
        UUID companyId,
        String planCode,
        SubscriptionStatus status,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        boolean autoRenew,
        boolean cancelAtPeriodEnd,
        String provider,
        String providerCustomerId,
        String providerSubscriptionId
) {
}
