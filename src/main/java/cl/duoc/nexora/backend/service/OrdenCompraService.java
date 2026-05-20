package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.OrdenCompraRequest;
import cl.duoc.nexora.backend.dto.response.OrdenCompraResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.OrdenCompraMapper;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.OrdenCompra;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.OrdenCompraRepository;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final SolicitudCompraRepository solicitudCompraRepository;
    private final CotizacionRepository cotizacionRepository;

    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listar() {
        return ordenCompraRepository.findAll().stream()
                .map(OrdenCompraMapper::toResponse)
                .toList();
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
        return OrdenCompraMapper.toResponse(ordenCompraRepository.save(ordenCompra));
    }

    @Transactional
    public OrdenCompraResponse actualizar(Long id, OrdenCompraRequest request) {
        OrdenCompra ordenCompra = buscarPorId(id);
        SolicitudCompra solicitudCompra = buscarSolicitud(request.solicitudCompraId());
        Cotizacion cotizacionGanadora = buscarCotizacion(request.cotizacionGanadoraId());
        validarCotizacionGanadora(solicitudCompra, cotizacionGanadora);
        OrdenCompraMapper.updateEntity(ordenCompra, request, solicitudCompra, cotizacionGanadora);
        return OrdenCompraMapper.toResponse(ordenCompraRepository.save(ordenCompra));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ordenCompraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden de compra no encontrada: " + id);
        }
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
}
