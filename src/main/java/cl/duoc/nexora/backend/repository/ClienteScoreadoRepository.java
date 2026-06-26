package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.ClienteScoreado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClienteScoreadoRepository extends JpaRepository<ClienteScoreado, Long> {

    @Query("SELECT c FROM ClienteScoreado c ORDER BY c.probAbandono DESC")
    List<ClienteScoreado> findAllOrderByRiescoDesc();
}
