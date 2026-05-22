package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.CotizacionRequest;
import cl.duoc.nexora.backend.dto.response.CotizacionResponse;
import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.CotizacionMapper;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.ProveedorRepository;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final SolicitudCompraRepository solicitudCompraRepository;
    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public Page<CotizacionResponse> listar(
            EstadoCotizacion estado,
            Long proveedorId,
            Long solicitudCompraId,
            Pageable pageable
    ) {
        return cotizacionRepository.buscar(estado, proveedorId, solicitudCompraId, pageable).map(CotizacionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtenerPorId(Long id) {
        return CotizacionMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public CotizacionResponse crear(CotizacionRequest request) {
        SolicitudCompra solicitudCompra = buscarSolicitud(request.solicitudCompraId());
        Proveedor proveedor = buscarProveedor(request.proveedorId());
        Cotizacion cotizacion = CotizacionMapper.toEntity(request, solicitudCompra, proveedor);
        log.info("Creando cotizacion para solicitud {} y proveedor {}", request.solicitudCompraId(), request.proveedorId());
        return CotizacionMapper.toResponse(cotizacionRepository.save(cotizacion));
    }

    @Transactional
    public CotizacionResponse actualizar(Long id, CotizacionRequest request) {
        Cotizacion cotizacion = buscarPorId(id);
        validarRelacionesInmutables(cotizacion, request);
        validarTransicionEstado(cotizacion.getEstado(), request.estado());
        CotizacionMapper.updateEntity(cotizacion, request);
        log.info("Actualizando cotizacion {}", id);
        return CotizacionMapper.toResponse(cotizacionRepository.save(cotizacion));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!cotizacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cotizacion no encontrada: " + id);
        }
        log.info("Eliminando cotizacion {}", id);
        cotizacionRepository.deleteById(id);
    }

    private Cotizacion buscarPorId(Long id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada: " + id));
    }

    private SolicitudCompra buscarSolicitud(Long solicitudCompraId) {
        return solicitudCompraRepository.findById(solicitudCompraId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de compra no encontrada: " + solicitudCompraId));
    }

    private Proveedor buscarProveedor(Long proveedorId) {
        return proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + proveedorId));
    }

    private void validarRelacionesInmutables(Cotizacion cotizacion, CotizacionRequest request) {
        if (!request.solicitudCompraId().equals(cotizacion.getSolicitudCompra().getId())) {
            throw new IllegalArgumentException("No se puede cambiar la solicitud de compra de una cotizacion existente");
        }
        if (!request.proveedorId().equals(cotizacion.getProveedor().getId())) {
            throw new IllegalArgumentException("No se puede cambiar el proveedor de una cotizacion existente");
        }
    }

    private void validarTransicionEstado(EstadoCotizacion actual, EstadoCotizacion nuevo) {
        if (nuevo == null || nuevo == actual) {
            return;
        }
        boolean valida = switch (actual) {
            case RECIBIDA -> nuevo == EstadoCotizacion.EN_REVISION
                    || nuevo == EstadoCotizacion.ACEPTADA
                    || nuevo == EstadoCotizacion.RECHAZADA;
            case EN_REVISION -> nuevo == EstadoCotizacion.ACEPTADA || nuevo == EstadoCotizacion.RECHAZADA;
            case ACEPTADA, RECHAZADA -> false;
        };
        if (!valida) {
            throw new IllegalArgumentException("Transicion de estado de cotizacion invalida: " + actual + " -> " + nuevo);
        }
    }
}
