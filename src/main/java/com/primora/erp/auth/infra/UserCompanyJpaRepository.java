package com.primora.erp.auth.infra;

import com.primora.erp.auth.domain.UserCompany;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCompanyJpaRepository extends JpaRepository<UserCompany, UUID> {

    Optional<UserCompany> findByUserId(UUID userId);
}
