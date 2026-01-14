package com.primora.erp.requisicoes.infra;

import com.primora.erp.requisicoes.domain.Comunicado;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunicadoJpaRepository extends JpaRepository<Comunicado, UUID> {

    Page<Comunicado> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Comunicado> findByCompanyIdAndId(UUID companyId, UUID id);
}
