package com.primora.erp.iam.infra;

import com.primora.erp.iam.domain.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCodeIgnoreCase(String code);

    Page<Role> findAll(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
            select r from Role r
            left join fetch r.permissions p
            where lower(r.code) = lower(:code)
            """)
    Optional<Role> findByCodeIgnoreCaseWithPermissions(@org.springframework.data.repository.query.Param("code")
                                                      String code);
}
