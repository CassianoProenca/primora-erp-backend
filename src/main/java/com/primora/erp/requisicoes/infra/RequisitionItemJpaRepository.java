package com.primora.erp.requisicoes.infra;

import com.primora.erp.requisicoes.domain.RequisitionItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitionItemJpaRepository extends JpaRepository<RequisitionItem, UUID> {

    List<RequisitionItem> findByRequisitionId(UUID requisitionId);
}
