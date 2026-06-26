package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.enums.AuthProvider;
import cl.duoc.nexora.backend.enums.RolUsuario;
import cl.duoc.nexora.backend.model.Usuario;
import cl.duoc.nexora.backend.repository.UsuarioRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario registrarOActualizarDesdeGoogle(OAuth2User oAuth2User) {
        String email = atributoRequerido(oAuth2User, "email");
        String nombre = atributoOpcional(oAuth2User, "name").orElse(email);
        String fotoUrl = atributoOpcional(oAuth2User, "picture").orElse(null);
        String providerId = atributoRequerido(oAuth2User, "sub");

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(Usuario::new);

        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setProvider(AuthProvider.GOOGLE);
        usuario.setProviderId(providerId);
        usuario.setFotoUrl(fotoUrl);
        if (usuario.getRol() == null) {
            usuario.setRol(RolUsuario.USER);
        }
        if (usuario.getActivo() == null) {
            usuario.setActivo(Boolean.TRUE);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerDesdeGoogle(OAuth2User oAuth2User) {
        return atributoOpcional(oAuth2User, "email")
                .flatMap(usuarioRepository::findByEmail);
    }

    private String atributoRequerido(OAuth2User oAuth2User, String nombre) {
        return atributoOpcional(oAuth2User, nombre)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("google_user_missing_" + nombre),
                        "Google no retorno el atributo requerido: " + nombre
                ));
    }

    private Optional<String> atributoOpcional(OAuth2User oAuth2User, String nombre) {
        Object valor = oAuth2User.getAttribute(nombre);
        if (valor instanceof String texto && !texto.isBlank()) {
            return Optional.of(texto);
        }
        return Optional.empty();
    }
}
