package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import cl.duoc.nexora.backend.model.Cotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    Page<Cotizacion> findByEstado(EstadoCotizacion estado, Pageable pageable);

    Page<Cotizacion> findByProveedorId(Long proveedorId, Pageable pageable);

    Page<Cotizacion> findBySolicitudCompraId(Long solicitudCompraId, Pageable pageable);

    @Query("""
            select c from Cotizacion c
            where (:estado is null or c.estado = :estado)
              and (:proveedorId is null or c.proveedor.id = :proveedorId)
              and (:solicitudCompraId is null or c.solicitudCompra.id = :solicitudCompraId)
            """)
    Page<Cotizacion> buscar(
            @Param("estado") EstadoCotizacion estado,
            @Param("proveedorId") Long proveedorId,
            @Param("solicitudCompraId") Long solicitudCompraId,
            Pageable pageable
    );
}
