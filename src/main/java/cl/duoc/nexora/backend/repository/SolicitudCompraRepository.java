package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudCompraRepository extends JpaRepository<SolicitudCompra, Long> {

    Page<SolicitudCompra> findByEstado(EstadoSolicitudCompra estado, Pageable pageable);

    Page<SolicitudCompra> findByUsuarioSolicitanteId(Long usuarioSolicitanteId, Pageable pageable);

    Page<SolicitudCompra> findByEstadoAndUsuarioSolicitanteId(
            EstadoSolicitudCompra estado,
            Long usuarioSolicitanteId,
            Pageable pageable
    );
}
