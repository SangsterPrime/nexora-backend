package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.NegociacionRequest;
import cl.duoc.nexora.backend.dto.response.NegociacionResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.NegociacionMapper;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.Negociacion;
import cl.duoc.nexora.backend.repository.CotizacionRepository;
import cl.duoc.nexora.backend.repository.NegociacionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NegociacionService {

    private final NegociacionRepository negociacionRepository;
    private final CotizacionRepository cotizacionRepository;

    @Transactional(readOnly = true)
    public List<NegociacionResponse> listar() {
        return negociacionRepository.findAll().stream()
                .map(NegociacionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NegociacionResponse obtenerPorId(Long id) {
        return NegociacionMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public NegociacionResponse crear(NegociacionRequest request) {
        Cotizacion cotizacion = buscarCotizacion(request.cotizacionId());
        Negociacion negociacion = NegociacionMapper.toEntity(request, cotizacion);
        return NegociacionMapper.toResponse(negociacionRepository.save(negociacion));
    }

    @Transactional
    public NegociacionResponse actualizar(Long id, NegociacionRequest request) {
        Negociacion negociacion = buscarPorId(id);
        Cotizacion cotizacion = buscarCotizacion(request.cotizacionId());
        NegociacionMapper.updateEntity(negociacion, request, cotizacion);
        return NegociacionMapper.toResponse(negociacionRepository.save(negociacion));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!negociacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Negociacion no encontrada: " + id);
        }
        negociacionRepository.deleteById(id);
    }

    private Negociacion buscarPorId(Long id) {
        return negociacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negociacion no encontrada: " + id));
    }

    private Cotizacion buscarCotizacion(Long cotizacionId) {
        return cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada: " + cotizacionId));
    }
}
