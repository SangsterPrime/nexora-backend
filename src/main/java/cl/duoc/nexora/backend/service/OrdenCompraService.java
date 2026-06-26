package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.OrdenCompraRequest;
import cl.duoc.nexora.backend.dto.response.OrdenCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoOrdenCompra;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.OrdenCompraMapper;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.OrdenCompra;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.OrdenCompraRepository;
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
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final SolicitudCompraRepository solicitudCompraRepository;
    private final CotizacionRepository cotizacionRepository;

    @Transactional(readOnly = true)
    public Page<OrdenCompraResponse> listar(
            EstadoOrdenCompra estado,
            Long proveedorId,
            Long solicitudCompraId,
            Pageable pageable
    ) {
        return ordenCompraRepository.buscar(estado, proveedorId, solicitudCompraId, pageable).map(OrdenCompraMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrdenCompraResponse obtenerPorId(Long id) {
        return OrdenCompraMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public OrdenCompraResponse crear(OrdenCompraRequest request) {
        SolicitudCompra solicitudCompra = buscarSolicitud(request.solicitudCompraId());
        Cotizacion cotizacionGanadora = buscarCotizacion(request.cotizacionGanadoraId());
        validarCotizacionGanadora(solicitudCompra, cotizacionGanadora);
        OrdenCompra ordenCompra = OrdenCompraMapper.toEntity(request, solicitudCompra, cotizacionGanadora);
        log.info("Creando orden de compra {}", request.numero());
        return OrdenCompraMapper.toResponse(ordenCompraRepository.save(ordenCompra));
    }

    @Transactional
    public OrdenCompraResponse actualizar(Long id, OrdenCompraRequest request) {
        OrdenCompra ordenCompra = buscarPorId(id);
        validarRelacionesInmutables(ordenCompra, request);
        validarTransicionEstado(ordenCompra.getEstado(), request.estado());
        OrdenCompraMapper.updateEntity(ordenCompra, request);
        log.info("Actualizando orden de compra {}", id);
        return OrdenCompraMapper.toResponse(ordenCompraRepository.save(ordenCompra));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ordenCompraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden de compra no encontrada: " + id);
        }
        log.info("Eliminando orden de compra {}", id);
        ordenCompraRepository.deleteById(id);
    }

    private OrdenCompra buscarPorId(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra no encontrada: " + id));
    }

    private SolicitudCompra buscarSolicitud(Long solicitudCompraId) {
        return solicitudCompraRepository.findById(solicitudCompraId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de compra no encontrada: " + solicitudCompraId));
    }

    private Cotizacion buscarCotizacion(Long cotizacionId) {
        return cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada: " + cotizacionId));
    }

    private void validarCotizacionGanadora(SolicitudCompra solicitudCompra, Cotizacion cotizacionGanadora) {
        Long solicitudCotizacionId = cotizacionGanadora.getSolicitudCompra().getId();
        if (!solicitudCompra.getId().equals(solicitudCotizacionId)) {
            throw new IllegalArgumentException("La cotizacion ganadora debe pertenecer a la solicitud de compra indicada");
        }
    }

    private void validarRelacionesInmutables(OrdenCompra ordenCompra, OrdenCompraRequest request) {
        if (!request.solicitudCompraId().equals(ordenCompra.getSolicitudCompra().getId())) {
            throw new IllegalArgumentException("No se puede cambiar la solicitud de compra de una orden existente");
        }
        if (!request.cotizacionGanadoraId().equals(ordenCompra.getCotizacionGanadora().getId())) {
            throw new IllegalArgumentException("No se puede cambiar la cotizacion ganadora de una orden existente");
        }
    }

    private void validarTransicionEstado(EstadoOrdenCompra actual, EstadoOrdenCompra nuevo) {
        if (nuevo == null || nuevo == actual) {
            return;
        }
        boolean valida = switch (actual) {
            case EMITIDA -> nuevo == EstadoOrdenCompra.APROBADA || nuevo == EstadoOrdenCompra.CANCELADA;
            case APROBADA -> nuevo == EstadoOrdenCompra.RECIBIDA || nuevo == EstadoOrdenCompra.CANCELADA;
            case RECIBIDA, CANCELADA -> false;
        };
        if (!valida) {
            throw new IllegalArgumentException("Transicion de estado de orden de compra invalida: " + actual + " -> " + nuevo);
        }
    }
}
