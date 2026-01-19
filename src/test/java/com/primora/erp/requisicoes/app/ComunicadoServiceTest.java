package com.primora.erp.requisicoes.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.requisicoes.domain.Comunicado;
import com.primora.erp.requisicoes.domain.ComunicadoStatus;
import com.primora.erp.requisicoes.infra.ComunicadoJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ComunicadoServiceTest {

    @Mock
    private ComunicadoJpaRepository comunicadoRepository;

    @InjectMocks
    private ComunicadoService comunicadoService;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createComunicado_ShouldSave() {
        UUID requesterDept = UUID.randomUUID();
        UUID targetDept = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();

        when(comunicadoRepository.save(any(Comunicado.class))).thenAnswer(i -> i.getArgument(0));

        Comunicado result = comunicadoService.createComunicado("Title", "Message", requesterDept, targetDept, authorId, recipientId);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Title");
        assertThat(result.getStatus()).isEqualTo(ComunicadoStatus.SENT);
        verify(comunicadoRepository).save(any(Comunicado.class));
    }

    @Test
    void markRead_ShouldUpdateStatus() {
        UUID comunicadoId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Comunicado comunicado = new Comunicado(comunicadoId, companyId, "T", "M", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), recipientId, ComunicadoStatus.SENT, Instant.now(), Instant.now());

        when(comunicadoRepository.findByCompanyIdAndId(companyId, comunicadoId)).thenReturn(Optional.of(comunicado));
        when(comunicadoRepository.save(any(Comunicado.class))).thenAnswer(i -> i.getArgument(0));

        Comunicado result = comunicadoService.markRead(comunicadoId, recipientId);

        assertThat(result.getStatus()).isEqualTo(ComunicadoStatus.READ);
        verify(comunicadoRepository).save(comunicado);
    }

    @Test
    void markRead_WhenNotRecipient_ShouldThrowForbidden() {
        UUID comunicadoId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Comunicado comunicado = new Comunicado(comunicadoId, companyId, "T", "M", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), recipientId, ComunicadoStatus.SENT, Instant.now(), Instant.now());

        when(comunicadoRepository.findByCompanyIdAndId(companyId, comunicadoId)).thenReturn(Optional.of(comunicado));

        assertThatThrownBy(() -> comunicadoService.markRead(comunicadoId, otherUserId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
