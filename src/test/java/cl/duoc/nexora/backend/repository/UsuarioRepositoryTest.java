package cl.duoc.nexora.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.nexora.backend.enums.RolUsuario;
import cl.duoc.nexora.backend.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void guardarYBuscarPorId() {
        Usuario usuario = usuario("ana@nexora.cl");

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        assertTrue(usuarioRepository.findById(guardado.getId()).isPresent());
        assertEquals("Ana Compras", usuarioRepository.findById(guardado.getId()).orElseThrow().getNombre());
    }

    @Test
    void emailDebeSerUnico() {
        usuarioRepository.saveAndFlush(usuario("ana@nexora.cl"));

        assertThrows(DataIntegrityViolationException.class, () ->
                usuarioRepository.saveAndFlush(usuario("ana@nexora.cl"))
        );
    }

    private Usuario usuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana Compras");
        usuario.setEmail(email);
        usuario.setRol(RolUsuario.COMPRADOR);
        usuario.setActivo(true);
        return usuario;
    }
}
