package com.primora.erp.iam.infra;

import com.primora.erp.iam.domain.UserCompanyRole;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCompanyRoleJpaRepository extends JpaRepository<UserCompanyRole, UUID> {

    boolean existsByUserIdAndCompanyIdAndRoleId(UUID userId, UUID companyId, UUID roleId);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Page<UserCompanyRole> findByUserIdAndCompanyId(UUID userId, UUID companyId, Pageable pageable);

    @Query("""
            select count(ucr) > 0 from UserCompanyRole ucr
            join ucr.role r
            join r.permissions p
            where ucr.userId = :userId
              and ucr.companyId = :companyId
              and lower(p.code) = lower(:code)
            """)
    boolean hasPermission(@Param("userId") UUID userId,
                          @Param("companyId") UUID companyId,
                          @Param("code") String code);
}
