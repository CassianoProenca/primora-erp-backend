package com.primora.erp.requisicoes.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.estoque.app.StockRequisitionService;
import com.primora.erp.requisicoes.domain.Requisition;
import com.primora.erp.requisicoes.domain.RequisitionStatus;
import com.primora.erp.requisicoes.infra.RequisitionItemJpaRepository;
import com.primora.erp.requisicoes.infra.RequisitionJpaRepository;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.Collections;
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
class RequisitionServiceTest {

    @Mock
    private RequisitionJpaRepository requisitionRepository;
    @Mock
    private RequisitionItemJpaRepository requisitionItemRepository;
    @Mock
    private StockRequisitionService stockRequisitionService;

    @InjectMocks
    private RequisitionService requisitionService;

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
    void createRequisition_ShouldSave() {
        UUID requesterDept = UUID.randomUUID();
        UUID targetDept = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();

        when(requisitionRepository.save(any(Requisition.class))).thenAnswer(i -> i.getArgument(0));

        Requisition result = requisitionService.createRequisition("Title", "Desc", requesterDept, targetDept, authorId, recipientId, 
                Collections.singletonList(new RequisitionService.ItemInput(UUID.randomUUID(), java.math.BigDecimal.TEN)));

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Title");
        assertThat(result.getStatus()).isEqualTo(RequisitionStatus.OPEN);
        verify(requisitionRepository).save(any(Requisition.class));
    }

    @Test
    void resolve_ShouldUpdateStatusAndConsumeStock() {
        UUID requisitionId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Requisition requisition = new Requisition(requisitionId, companyId, "T", "D", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), recipientId, RequisitionStatus.OPEN, false, Instant.now(), Instant.now());

        when(requisitionRepository.findByCompanyIdAndIdAndDeletedFalse(companyId, requisitionId)).thenReturn(Optional.of(requisition));
        when(requisitionRepository.save(any(Requisition.class))).thenAnswer(i -> i.getArgument(0));
        when(requisitionItemRepository.findByRequisitionId(requisitionId)).thenReturn(Collections.emptyList());

        Requisition result = requisitionService.resolve(requisitionId, recipientId);

        assertThat(result.getStatus()).isEqualTo(RequisitionStatus.RESOLVED);
        verify(stockRequisitionService).consumeForRequisition(requisition, Collections.emptyList(), recipientId);
        verify(requisitionRepository).save(requisition);
    }
}
