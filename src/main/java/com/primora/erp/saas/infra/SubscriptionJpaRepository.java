package com.primora.erp.saas.infra;

import com.primora.erp.saas.domain.Subscription;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByCompanyId(UUID companyId);
    List<Subscription> findByCompanyIdIn(Collection<UUID> companyIds);
}
