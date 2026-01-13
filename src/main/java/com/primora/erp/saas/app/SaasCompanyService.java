package com.primora.erp.saas.app;

import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.onboarding.infra.CompanyJpaRepository;
import com.primora.erp.saas.domain.Subscription;
import com.primora.erp.saas.domain.SubscriptionStatus;
import com.primora.erp.saas.infra.SubscriptionJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SaasCompanyService {

    private final CompanyJpaRepository companyRepository;
    private final SubscriptionJpaRepository subscriptionRepository;

    public SaasCompanyService(CompanyJpaRepository companyRepository,
                              SubscriptionJpaRepository subscriptionRepository) {
        this.companyRepository = companyRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public Page<SaasCompanySummary> listCompanies(String status, Pageable pageable) {
        String targetStatus = status == null || status.isBlank() ? "ACTIVE" : status;
        Page<Company> companies = companyRepository.findByStatusIgnoreCase(targetStatus, pageable);
        List<UUID> companyIds = companies.getContent().stream()
                .map(Company::getId)
                .toList();
        Map<UUID, Subscription> subscriptions = loadSubscriptions(companyIds);

        return companies.map(company -> new SaasCompanySummary(
                company.getId(),
                company.getLegalName(),
                company.getTradeName(),
                company.getDocument(),
                company.getStatus(),
                company.getOnboardingStatus(),
                company.getPrimaryAdminUserId(),
                subscriptionStatus(subscriptions.get(company.getId()))
        ));
    }

    @Transactional(readOnly = true)
    public Subscription getSubscription(UUID companyId) {
        return subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }

    @Transactional(readOnly = true)
    public Company getCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }

    private Map<UUID, Subscription> loadSubscriptions(Collection<UUID> companyIds) {
        if (companyIds.isEmpty()) {
            return Map.of();
        }
        List<Subscription> subscriptions = subscriptionRepository.findByCompanyIdIn(companyIds);
        return subscriptions.stream()
                .collect(Collectors.toMap(Subscription::getCompanyId, Function.identity()));
    }

    private SubscriptionStatus subscriptionStatus(Subscription subscription) {
        return subscription == null ? null : subscription.getStatus();
    }
}
