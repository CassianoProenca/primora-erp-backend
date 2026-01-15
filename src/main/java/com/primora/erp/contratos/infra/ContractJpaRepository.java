package com.primora.erp.contratos.infra;

import com.primora.erp.contratos.domain.Contract;
import com.primora.erp.contratos.domain.ContractStatus;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractJpaRepository extends JpaRepository<Contract, UUID> {

    Page<Contract> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Contract> findByCompanyIdAndId(UUID companyId, UUID id);

    @Query("select c from Contract c "
            + "where c.companyId = :companyId "
            + "and c.status = :status "
            + "and c.endDate is not null "
            + "and c.endDate between :today and :limit")
    Page<Contract> findExpiringContracts(@Param("companyId") UUID companyId,
                                         @Param("status") ContractStatus status,
                                         @Param("today") LocalDate today,
                                         @Param("limit") LocalDate limit,
                                         Pageable pageable);
}
