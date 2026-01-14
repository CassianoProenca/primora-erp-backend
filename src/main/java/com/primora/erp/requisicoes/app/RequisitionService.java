package com.primora.erp.requisicoes.app;

import com.primora.erp.requisicoes.domain.Requisition;
import com.primora.erp.requisicoes.domain.RequisitionItem;
import com.primora.erp.requisicoes.domain.RequisitionStatus;
import com.primora.erp.requisicoes.infra.RequisitionItemJpaRepository;
import com.primora.erp.requisicoes.infra.RequisitionJpaRepository;
import com.primora.erp.estoque.app.StockRequisitionService;
import com.primora.erp.shared.security.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RequisitionService {

    private final RequisitionJpaRepository requisitionRepository;
    private final RequisitionItemJpaRepository requisitionItemRepository;
    private final StockRequisitionService stockRequisitionService;

    public RequisitionService(RequisitionJpaRepository requisitionRepository,
                              RequisitionItemJpaRepository requisitionItemRepository,
                              StockRequisitionService stockRequisitionService) {
        this.requisitionRepository = requisitionRepository;
        this.requisitionItemRepository = requisitionItemRepository;
        this.stockRequisitionService = stockRequisitionService;
    }

    @Transactional
    public Requisition createRequisition(String title, String description,
                                         UUID requesterDepartmentId, UUID targetDepartmentId,
                                         UUID authorUserId, UUID recipientUserId,
                                         List<ItemInput> items) {
        UUID companyId = requireCompanyId();
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requisition items are required");
        }
        Instant now = Instant.now();
        Requisition requisition = new Requisition(
                UUID.randomUUID(),
                companyId,
                title,
                description,
                requesterDepartmentId,
                targetDepartmentId,
                authorUserId,
                recipientUserId,
                RequisitionStatus.OPEN,
                false,
                now,
                now
        );
        Requisition saved = requisitionRepository.save(requisition);
        if (items != null && !items.isEmpty()) {
            List<RequisitionItem> requisitionItems = items.stream()
                    .map(item -> new RequisitionItem(
                            UUID.randomUUID(),
                            saved.getId(),
                            item.itemId(),
                            item.quantity()
                    ))
                    .toList();
            requisitionItemRepository.saveAll(requisitionItems);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Requisition> listRequisitions(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return requisitionRepository.findByCompanyIdAndDeletedFalse(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public Requisition getRequisition(UUID requisitionId) {
        UUID companyId = requireCompanyId();
        return requisitionRepository.findByCompanyIdAndIdAndDeletedFalse(companyId, requisitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisition not found"));
    }

    @Transactional
    public Requisition markRead(UUID requisitionId, UUID actorUserId) {
        Requisition requisition = getRequisition(requisitionId);
        enforceRecipient(requisition, actorUserId);
        if (requisition.getStatus() == RequisitionStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requisition already resolved");
        }
        if (requisition.getStatus() == RequisitionStatus.OPEN) {
            requisition.markRead(Instant.now());
            return requisitionRepository.save(requisition);
        }
        return requisition;
    }

    @Transactional
    public Requisition resolve(UUID requisitionId, UUID actorUserId) {
        Requisition requisition = getRequisition(requisitionId);
        enforceRecipient(requisition, actorUserId);
        if (requisition.getStatus() == RequisitionStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requisition already resolved");
        }
        if (requisition.getStatus() == RequisitionStatus.OPEN) {
            requisition.markRead(Instant.now());
        }
        List<RequisitionItem> items = requisitionItemRepository.findByRequisitionId(requisition.getId());
        stockRequisitionService.consumeForRequisition(requisition, items, actorUserId);
        requisition.markResolved(Instant.now());
        return requisitionRepository.save(requisition);
    }

    @Transactional
    public void delete(UUID requisitionId) {
        Requisition requisition = getRequisition(requisitionId);
        if (requisition.isDeleted()) {
            return;
        }
        requisition.markDeleted(Instant.now());
        requisitionRepository.save(requisition);
    }

    @Transactional(readOnly = true)
    public List<RequisitionItem> listItems(UUID requisitionId) {
        getRequisition(requisitionId);
        return requisitionItemRepository.findByRequisitionId(requisitionId);
    }

    private void enforceRecipient(Requisition requisition, UUID actorUserId) {
        UUID recipient = requisition.getRecipientUserId();
        if (recipient != null && !recipient.equals(actorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can update this requisition");
        }
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }

    public record ItemInput(UUID itemId, BigDecimal quantity) {
    }
}
