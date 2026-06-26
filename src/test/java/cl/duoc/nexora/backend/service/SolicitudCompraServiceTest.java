package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.dto.integration.N8nEventRequest;
import cl.duoc.nexora.backend.dto.request.SolicitudCompraRequest;
import cl.duoc.nexora.backend.dto.response.SolicitudCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.enums.RolUsuario;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.model.Usuario;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import cl.duoc.nexora.backend.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SolicitudCompraServiceTest {

    @Mock
    private SolicitudCompraRepository solicitudCompraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private N8nIntegrationService n8nIntegrationService;

    @InjectMocks
    private SolicitudCompraService solicitudCompraService;

    @Test
    void crearSolicitudAsociadaAUsuarioExistente() {
        Usuario usuario = usuario(1L);
        SolicitudCompraRequest request = solicitudRequest(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(solicitudCompraRepository.save(any(SolicitudCompra.class))).thenAnswer(invocation -> {
            SolicitudCompra solicitud = invocation.getArgument(0);
            solicitud.setId(10L);
            return solicitud;
        });

        SolicitudCompraResponse response = solicitudCompraService.crear(request);

        assertEquals(10L, response.id());
        assertEquals(1L, response.usuarioSolicitanteId());
        assertEquals("Ana Compras", response.usuarioSolicitanteNombre());
        verify(solicitudCompraRepository).save(any(SolicitudCompra.class));
    }

    @Test
    void crearSolicitudFallaSiUsuarioSolicitanteNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> solicitudCompraService.crear(solicitudRequest(99L)));
    }

    @Test
    void crearSolicitudNoFallaAunqueN8nEsteCalido() {
        // Dado que n8n lanza excepción, la solicitud debe crearse igualmente
        Usuario usuario = usuario(1L);
        SolicitudCompraRequest request = solicitudRequest(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(solicitudCompraRepository.save(any(SolicitudCompra.class))).thenAnswer(invocation -> {
            SolicitudCompra solicitud = invocation.getArgument(0);
            solicitud.setId(20L);
            return solicitud;
        });
        when(n8nIntegrationService.enviarEvento(any(N8nEventRequest.class)))
                .thenThrow(new RuntimeException("n8n caído — conexión rechazada"));

        // No debe lanzar excepción
        SolicitudCompraResponse response = solicitudCompraService.crear(request);

        assertNotNull(response);
        assertEquals(20L, response.id());
        verify(solicitudCompraRepository).save(any(SolicitudCompra.class));
    }

    private SolicitudCompraRequest solicitudRequest(Long usuarioId) {
        return new SolicitudCompraRequest(
                "Compra de notebooks",
                "Equipos para equipo de compras",
                "Tecnologia",
                BigDecimal.valueOf(2_500_000),
                LocalDate.now().plusDays(15),
                EstadoSolicitudCompra.ABIERTA,
                usuarioId
        );
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
