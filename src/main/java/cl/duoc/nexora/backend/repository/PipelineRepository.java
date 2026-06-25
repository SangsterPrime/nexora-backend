package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.Pipeline;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    Optional<Pipeline> findFirstByNombre(String nombre);
}
