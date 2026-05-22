package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findByActivo(Boolean activo, Pageable pageable);
}
