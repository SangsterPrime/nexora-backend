package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.dto.request.CotizacionRequest;
import cl.duoc.nexora.backend.dto.response.CotizacionResponse;
import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.ProveedorRepository;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private SolicitudCompraRepository solicitudCompraRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private CotizacionService cotizacionService;

    @Test
    void crearCotizacionAsociadaASolicitudYProveedorExistentes() {
        SolicitudCompra solicitud = solicitud(1L);
        Proveedor proveedor = proveedor(2L);
        CotizacionRequest request = cotizacionRequest(1L, 2L);
        when(solicitudCompraRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(proveedorRepository.findById(2L)).thenReturn(Optional.of(proveedor));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> {
            Cotizacion cotizacion = invocation.getArgument(0);
            cotizacion.setId(5L);
            return cotizacion;
        });

        CotizacionResponse response = cotizacionService.crear(request);

        assertEquals(5L, response.id());
        assertEquals(1L, response.solicitudCompraId());
        assertEquals(2L, response.proveedorId());
        verify(cotizacionRepository).save(any(Cotizacion.class));
    }

    @Test
    void crearCotizacionFallaSiSolicitudNoExiste() {
        when(solicitudCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cotizacionService.crear(cotizacionRequest(99L, 2L)));
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
    }

    @Test
    void crearCotizacionFallaSiProveedorNoExiste() {
        when(solicitudCompraRepository.findById(1L)).thenReturn(Optional.of(solicitud(1L)));
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cotizacionService.crear(cotizacionRequest(1L, 99L)));
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
    }

    private CotizacionRequest cotizacionRequest(Long solicitudId, Long proveedorId) {
        return new CotizacionRequest(
                solicitudId,
                proveedorId,
                BigDecimal.valueOf(1_800_000),
                10,
                "Pago a 30 dias",
                BigDecimal.valueOf(15),
                EstadoCotizacion.RECIBIDA
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
