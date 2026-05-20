package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.CotizacionRequest;
import cl.duoc.nexora.backend.dto.response.CotizacionResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.CotizacionMapper;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.ProveedorRepository;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final SolicitudCompraRepository solicitudCompraRepository;
    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listar() {
        return cotizacionRepository.findAll().stream()
                .map(CotizacionMapper::toResponse)
                .toList();
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
        return CotizacionMapper.toResponse(cotizacionRepository.save(cotizacion));
    }

    @Transactional
    public CotizacionResponse actualizar(Long id, CotizacionRequest request) {
        Cotizacion cotizacion = buscarPorId(id);
        SolicitudCompra solicitudCompra = buscarSolicitud(request.solicitudCompraId());
        Proveedor proveedor = buscarProveedor(request.proveedorId());
        CotizacionMapper.updateEntity(cotizacion, request, solicitudCompra, proveedor);
        return CotizacionMapper.toResponse(cotizacionRepository.save(cotizacion));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!cotizacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cotizacion no encontrada: " + id);
        }
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
}
