package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.MlMetrica;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlMetricaRepository extends JpaRepository<MlMetrica, Long> {

    /** Última corrida de métricas registrada (la más reciente por {@code ts}). */
    Optional<MlMetrica> findFirstByOrderByTsDesc();
}
