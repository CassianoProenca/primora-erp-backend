package com.primora.erp.requisicoes.app;

import com.primora.erp.requisicoes.domain.Comunicado;
import com.primora.erp.requisicoes.domain.ComunicadoStatus;
import com.primora.erp.requisicoes.infra.ComunicadoJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComunicadoService {

    private final ComunicadoJpaRepository comunicadoRepository;

    public ComunicadoService(ComunicadoJpaRepository comunicadoRepository) {
        this.comunicadoRepository = comunicadoRepository;
    }

    @Transactional
    public Comunicado createComunicado(String title, String message,
                                       UUID requesterDepartmentId, UUID targetDepartmentId,
                                       UUID authorUserId, UUID recipientUserId) {
        UUID companyId = requireCompanyId();
        Instant now = Instant.now();
        Comunicado comunicado = new Comunicado(
                UUID.randomUUID(),
                companyId,
                title,
                message,
                requesterDepartmentId,
                targetDepartmentId,
                authorUserId,
                recipientUserId,
                ComunicadoStatus.SENT,
                now,
                now
        );
        return comunicadoRepository.save(comunicado);
    }

    @Transactional(readOnly = true)
    public Page<Comunicado> listComunicados(Pageable pageable) {
        UUID companyId = requireCompanyId();
        return comunicadoRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public Comunicado getComunicado(UUID comunicadoId) {
        UUID companyId = requireCompanyId();
        return comunicadoRepository.findByCompanyIdAndId(companyId, comunicadoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunicado not found"));
    }

    @Transactional
    public Comunicado markReceived(UUID comunicadoId, UUID actorUserId) {
        Comunicado comunicado = getComunicado(comunicadoId);
        enforceRecipient(comunicado, actorUserId);
        if (comunicado.getStatus() == ComunicadoStatus.SENT) {
            comunicado.markReceived(Instant.now());
            return comunicadoRepository.save(comunicado);
        }
        return comunicado;
    }

    @Transactional
    public Comunicado markRead(UUID comunicadoId, UUID actorUserId) {
        Comunicado comunicado = getComunicado(comunicadoId);
        enforceRecipient(comunicado, actorUserId);
        if (comunicado.getStatus() != ComunicadoStatus.READ) {
            Instant now = Instant.now();
            if (comunicado.getStatus() == ComunicadoStatus.SENT) {
                comunicado.markReceived(now);
            }
            comunicado.markRead(now);
            return comunicadoRepository.save(comunicado);
        }
        return comunicado;
    }

    private void enforceRecipient(Comunicado comunicado, UUID actorUserId) {
        UUID recipient = comunicado.getRecipientUserId();
        if (recipient != null && !recipient.equals(actorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can update this comunicado");
        }
    }

    private UUID requireCompanyId() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }
        return companyId;
    }
}
