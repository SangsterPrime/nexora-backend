package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.enums.EstadoOrdenCompra;
import cl.duoc.nexora.backend.model.OrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    Page<OrdenCompra> findByEstado(EstadoOrdenCompra estado, Pageable pageable);

    Page<OrdenCompra> findBySolicitudCompraId(Long solicitudCompraId, Pageable pageable);

    Page<OrdenCompra> findByCotizacionGanadoraProveedorId(Long proveedorId, Pageable pageable);

    @Query("""
            select o from OrdenCompra o
            where (:estado is null or o.estado = :estado)
              and (:proveedorId is null or o.cotizacionGanadora.proveedor.id = :proveedorId)
              and (:solicitudCompraId is null or o.solicitudCompra.id = :solicitudCompraId)
            """)
    Page<OrdenCompra> buscar(
            @Param("estado") EstadoOrdenCompra estado,
            @Param("proveedorId") Long proveedorId,
            @Param("solicitudCompraId") Long solicitudCompraId,
            Pageable pageable
    );
}
