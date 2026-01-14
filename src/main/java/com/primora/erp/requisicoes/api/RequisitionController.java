package com.primora.erp.requisicoes.api;

import com.primora.erp.requisicoes.api.dto.RequisitionItemResponse;
import com.primora.erp.requisicoes.api.dto.RequisitionRequest;
import com.primora.erp.requisicoes.api.dto.RequisitionResponse;
import com.primora.erp.requisicoes.app.RequisitionService;
import com.primora.erp.requisicoes.domain.Requisition;
import com.primora.erp.requisicoes.domain.RequisitionItem;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/requisicoes")
public class RequisitionController {

    private final RequisitionService requisitionService;
    private final AuditService auditService;

    public RequisitionController(RequisitionService requisitionService, AuditService auditService) {
        this.requisitionService = requisitionService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<RequisitionResponse> create(@Valid @RequestBody RequisitionRequest request) {
        JwtUser user = currentUser();
        Requisition requisition = requisitionService.createRequisition(
                request.title(),
                request.description(),
                request.requesterDepartmentId(),
                request.targetDepartmentId(),
                user.userId(),
                request.recipientUserId(),
                request.items().stream()
                        .map(item -> new RequisitionService.ItemInput(item.itemId(), item.quantity()))
                        .toList()
        );
        auditService.log(
                "REQUISITION_CREATED",
                user.userId(),
                user.companyId(),
                "{\"requisitionId\":\"" + requisition.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(requisition));
    }

    @GetMapping
    public ResponseEntity<Page<RequisitionResponse>> list(Pageable pageable) {
        JwtUser user = currentUser();
        Page<RequisitionResponse> requisitions = requisitionService.listRequisitions(pageable)
                .map(this::toResponse);
        auditService.log(
                "REQUISITION_LIST_VIEWED",
                user.userId(),
                user.companyId(),
                "{}"
        );
        return ResponseEntity.ok(requisitions);
    }

    @GetMapping("/{requisitionId}")
    public ResponseEntity<RequisitionResponse> get(@PathVariable UUID requisitionId) {
        JwtUser user = currentUser();
        Requisition requisition = requisitionService.getRequisition(requisitionId);
        auditService.log(
                "REQUISITION_VIEWED",
                user.userId(),
                user.companyId(),
                "{\"requisitionId\":\"" + requisitionId + "\"}"
        );
        return ResponseEntity.ok(toResponse(requisition));
    }

    @PostMapping("/{requisitionId}/read")
    public ResponseEntity<RequisitionResponse> markRead(@PathVariable UUID requisitionId) {
        JwtUser user = currentUser();
        Requisition requisition = requisitionService.markRead(requisitionId, user.userId());
        auditService.log(
                "REQUISITION_READ",
                user.userId(),
                user.companyId(),
                "{\"requisitionId\":\"" + requisitionId + "\"}"
        );
        return ResponseEntity.ok(toResponse(requisition));
    }

    @PostMapping("/{requisitionId}/resolve")
    public ResponseEntity<RequisitionResponse> resolve(@PathVariable UUID requisitionId) {
        JwtUser user = currentUser();
        Requisition requisition = requisitionService.resolve(requisitionId, user.userId());
        auditService.log(
                "REQUISITION_RESOLVED",
                user.userId(),
                user.companyId(),
                "{\"requisitionId\":\"" + requisitionId + "\"}"
        );
        return ResponseEntity.ok(toResponse(requisition));
    }

    @DeleteMapping("/{requisitionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID requisitionId) {
        JwtUser user = currentUser();
        requisitionService.delete(requisitionId);
        auditService.log(
                "REQUISITION_DELETED",
                user.userId(),
                user.companyId(),
                "{\"requisitionId\":\"" + requisitionId + "\"}"
        );
        return ResponseEntity.noContent().build();
    }

    private RequisitionResponse toResponse(Requisition requisition) {
        List<RequisitionItemResponse> items = requisitionService.listItems(requisition.getId()).stream()
                .map(this::toItemResponse)
                .toList();
        return new RequisitionResponse(
                requisition.getId(),
                requisition.getTitle(),
                requisition.getDescription(),
                requisition.getRequesterDepartmentId(),
                requisition.getTargetDepartmentId(),
                requisition.getAuthorUserId(),
                requisition.getRecipientUserId(),
                requisition.getStatus(),
                items,
                requisition.getCreatedAt(),
                requisition.getUpdatedAt()
        );
    }

    private RequisitionItemResponse toItemResponse(RequisitionItem item) {
        return new RequisitionItemResponse(item.getId(), item.getItemId(), item.getQuantity());
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
