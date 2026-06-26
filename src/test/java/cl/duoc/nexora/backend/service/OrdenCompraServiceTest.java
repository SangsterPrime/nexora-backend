package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.dto.request.OrdenCompraRequest;
import cl.duoc.nexora.backend.dto.response.OrdenCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import cl.duoc.nexora.backend.enums.EstadoOrdenCompra;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.OrdenCompra;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.OrdenCompraRepository;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private SolicitudCompraRepository solicitudCompraRepository;

    @Mock
    private CotizacionRepository cotizacionRepository;

    @InjectMocks
    private OrdenCompraService ordenCompraService;

    @Test
    void crearOrdenCompraConSolicitudYCotizacionGanadoraValidas() {
        SolicitudCompra solicitud = solicitud(1L);
        Cotizacion cotizacion = cotizacion(2L, solicitud);
        OrdenCompraRequest request = ordenRequest(1L, 2L);
        when(solicitudCompraRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(cotizacionRepository.findById(2L)).thenReturn(Optional.of(cotizacion));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(invocation -> {
            OrdenCompra ordenCompra = invocation.getArgument(0);
            ordenCompra.setId(20L);
            return ordenCompra;
        });

        OrdenCompraResponse response = ordenCompraService.crear(request);

        assertEquals(20L, response.id());
        assertEquals("OC-0001", response.numero());
        assertEquals(1L, response.solicitudCompraId());
        assertEquals(2L, response.cotizacionGanadoraId());
        verify(ordenCompraRepository).save(any(OrdenCompra.class));
    }

    @Test
    void crearOrdenCompraLanzaIllegalArgumentExceptionSiCotizacionNoPerteneceALaSolicitud() {
        SolicitudCompra solicitudOrden = solicitud(1L);
        SolicitudCompra otraSolicitud = solicitud(99L);
        Cotizacion cotizacion = cotizacion(2L, otraSolicitud);
        when(solicitudCompraRepository.findById(1L)).thenReturn(Optional.of(solicitudOrden));
        when(cotizacionRepository.findById(2L)).thenReturn(Optional.of(cotizacion));

        assertThrows(IllegalArgumentException.class, () -> ordenCompraService.crear(ordenRequest(1L, 2L)));
        verify(ordenCompraRepository, never()).save(any(OrdenCompra.class));
    }

    private OrdenCompraRequest ordenRequest(Long solicitudId, Long cotizacionId) {
        return new OrdenCompraRequest(
                "OC-0001",
                solicitudId,
                cotizacionId,
                BigDecimal.valueOf(1_800_000),
                EstadoOrdenCompra.EMITIDA
        );
    }

    private SolicitudCompra solicitud(Long id) {
        SolicitudCompra solicitud = new SolicitudCompra();
        solicitud.setId(id);
        solicitud.setTitulo("Compra de notebooks");
        solicitud.setMontoEstimado(BigDecimal.valueOf(2_500_000));
        solicitud.setEstado(EstadoSolicitudCompra.ABIERTA);
        return solicitud;
    }

    private Cotizacion cotizacion(Long id, SolicitudCompra solicitud) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(id);
        cotizacion.setSolicitudCompra(solicitud);
        cotizacion.setProveedor(proveedor(3L));
        cotizacion.setMonto(BigDecimal.valueOf(1_800_000));
        cotizacion.setEstado(EstadoCotizacion.ACEPTADA);
        return cotizacion;
    }

    private Proveedor proveedor(Long id) {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setRut("76000000-1");
        proveedor.setRazonSocial("Proveedor SpA");
        proveedor.setEmail("proveedor@nexora.cl");
        proveedor.setEstado(EstadoProveedor.ACTIVO);
        return proveedor;
    }
}
