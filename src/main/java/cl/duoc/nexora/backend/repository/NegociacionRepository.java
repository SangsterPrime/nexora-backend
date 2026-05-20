package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.Negociacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegociacionRepository extends JpaRepository<Negociacion, Long> {
}
