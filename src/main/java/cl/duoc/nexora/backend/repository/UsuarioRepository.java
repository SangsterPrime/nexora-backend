package cl.duoc.nexora.backend.repository;

import cl.duoc.nexora.backend.enums.AuthProvider;
import cl.duoc.nexora.backend.model.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findByActivo(Boolean activo, Pageable pageable);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
