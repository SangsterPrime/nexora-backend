package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.MlPrediccion;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlPrediccionRepository extends JpaRepository<MlPrediccion, Long> {

    /** Últimas predicciones por {@code ts DESC}; el límite se controla con {@link Pageable}. */
    List<MlPrediccion> findByOrderByTsDesc(Pageable pageable);
}
