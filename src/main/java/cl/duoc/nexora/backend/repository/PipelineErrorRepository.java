package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.PipelineError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineErrorRepository extends JpaRepository<PipelineError, Long> {
}
