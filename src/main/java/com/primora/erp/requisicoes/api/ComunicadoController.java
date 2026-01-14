package com.primora.erp.requisicoes.api;

import com.primora.erp.requisicoes.api.dto.ComunicadoRequest;
import com.primora.erp.requisicoes.api.dto.ComunicadoResponse;
import com.primora.erp.requisicoes.app.ComunicadoService;
import com.primora.erp.requisicoes.domain.Comunicado;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/comunicados")
public class ComunicadoController {

    private final ComunicadoService comunicadoService;
    private final AuditService auditService;

    public ComunicadoController(ComunicadoService comunicadoService, AuditService auditService) {
        this.comunicadoService = comunicadoService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<ComunicadoResponse> create(@Valid @RequestBody ComunicadoRequest request) {
        JwtUser user = currentUser();
        Comunicado comunicado = comunicadoService.createComunicado(
                request.title(),
                request.message(),
                request.requesterDepartmentId(),
                request.targetDepartmentId(),
                user.userId(),
                request.recipientUserId()
        );
        auditService.log(
                "COMUNICADO_CREATED",
                user.userId(),
                user.companyId(),
                "{\"comunicadoId\":\"" + comunicado.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(comunicado));
    }

    @GetMapping
    public ResponseEntity<Page<ComunicadoResponse>> list(Pageable pageable) {
        Page<ComunicadoResponse> comunicados = comunicadoService.listComunicados(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(comunicados);
    }

    @GetMapping("/{comunicadoId}")
    public ResponseEntity<ComunicadoResponse> get(@PathVariable UUID comunicadoId) {
        JwtUser user = currentUser();
        Comunicado comunicado = comunicadoService.getComunicado(comunicadoId);
        auditService.log(
                "COMUNICADO_VIEWED",
                user.userId(),
                user.companyId(),
                "{\"comunicadoId\":\"" + comunicadoId + "\"}"
        );
        return ResponseEntity.ok(toResponse(comunicado));
    }

    @PostMapping("/{comunicadoId}/received")
    public ResponseEntity<ComunicadoResponse> markReceived(@PathVariable UUID comunicadoId) {
        JwtUser user = currentUser();
        Comunicado comunicado = comunicadoService.markReceived(comunicadoId, user.userId());
        auditService.log(
                "COMUNICADO_RECEIVED",
                user.userId(),
                user.companyId(),
                "{\"comunicadoId\":\"" + comunicadoId + "\"}"
        );
        return ResponseEntity.ok(toResponse(comunicado));
    }

    @PostMapping("/{comunicadoId}/read")
    public ResponseEntity<ComunicadoResponse> markRead(@PathVariable UUID comunicadoId) {
        JwtUser user = currentUser();
        Comunicado comunicado = comunicadoService.markRead(comunicadoId, user.userId());
        auditService.log(
                "COMUNICADO_READ",
                user.userId(),
                user.companyId(),
                "{\"comunicadoId\":\"" + comunicadoId + "\"}"
        );
        return ResponseEntity.ok(toResponse(comunicado));
    }

    private ComunicadoResponse toResponse(Comunicado comunicado) {
        return new ComunicadoResponse(
                comunicado.getId(),
                comunicado.getTitle(),
                comunicado.getMessage(),
                comunicado.getRequesterDepartmentId(),
                comunicado.getTargetDepartmentId(),
                comunicado.getAuthorUserId(),
                comunicado.getRecipientUserId(),
                comunicado.getStatus(),
                comunicado.getCreatedAt(),
                comunicado.getUpdatedAt()
        );
    }

    private JwtUser currentUser() {
        return CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}
