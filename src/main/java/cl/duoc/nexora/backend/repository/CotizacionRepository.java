package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
}
