package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.dto.request.UsuarioRequest;
import cl.duoc.nexora.backend.dto.response.UsuarioResponse;
import cl.duoc.nexora.backend.enums.RolUsuario;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.model.Usuario;
import cl.duoc.nexora.backend.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuarioCorrectamente() {
        UsuarioRequest request = new UsuarioRequest("Ana Compras", "ana@nexora.cl", RolUsuario.COMPRADOR, true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponse response = usuarioService.crear(request);

        assertEquals(1L, response.id());
        assertEquals("Ana Compras", response.nombre());
        assertEquals("ana@nexora.cl", response.email());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void obtenerUsuarioPorIdExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));

        UsuarioResponse response = usuarioService.obtenerPorId(1L);

        assertEquals(1L, response.id());
        assertEquals(RolUsuario.COMPRADOR, response.rol());
    }

    @Test
    void obtenerUsuarioPorIdInexistenteLanzaResourceNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.obtenerPorId(99L));
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Ana Compras");
        usuario.setEmail("ana@nexora.cl");
        usuario.setRol(RolUsuario.COMPRADOR);
        usuario.setActivo(true);
        return usuario;
    }
}
