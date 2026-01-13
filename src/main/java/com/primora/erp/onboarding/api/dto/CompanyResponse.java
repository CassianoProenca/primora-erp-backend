package com.primora.erp.onboarding.api.dto;

import com.primora.erp.onboarding.domain.OnboardingStatus;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String legalName,
        String tradeName,
        String document,
        OnboardingStatus onboardingStatus
) {
}
