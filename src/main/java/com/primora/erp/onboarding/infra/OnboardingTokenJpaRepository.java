package com.primora.erp.onboarding.infra;

import com.primora.erp.onboarding.domain.OnboardingToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingTokenJpaRepository extends JpaRepository<OnboardingToken, UUID> {
}
