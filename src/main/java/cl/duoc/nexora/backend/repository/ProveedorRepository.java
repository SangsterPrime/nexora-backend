package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.model.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Page<Proveedor> findByEstado(EstadoProveedor estado, Pageable pageable);
}
