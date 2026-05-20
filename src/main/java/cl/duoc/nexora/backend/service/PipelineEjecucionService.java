package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.PipelineEjecucionRequest;
import cl.duoc.nexora.backend.dto.response.PipelineEjecucionResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.PipelineEjecucionMapper;
import cl.duoc.nexora.backend.model.Pipeline;
import cl.duoc.nexora.backend.model.PipelineEjecucion;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.repository.PipelineEjecucionRepository;
import cl.duoc.nexora.backend.repository.PipelineRepository;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PipelineEjecucionService {

    private final PipelineEjecucionRepository pipelineEjecucionRepository;
    private final PipelineRepository pipelineRepository;
    private final SolicitudCompraRepository solicitudCompraRepository;

    @Transactional(readOnly = true)
    public List<PipelineEjecucionResponse> listar() {
        return pipelineEjecucionRepository.findAll().stream()
                .map(PipelineEjecucionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PipelineEjecucionResponse obtenerPorId(Long id) {
        return PipelineEjecucionMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public PipelineEjecucionResponse crear(PipelineEjecucionRequest request) {
        Pipeline pipeline = buscarPipeline(request.pipelineId());
        SolicitudCompra solicitudCompra = buscarSolicitudOpcional(request.solicitudCompraId());
        PipelineEjecucion ejecucion = PipelineEjecucionMapper.toEntity(request, pipeline, solicitudCompra);
        return PipelineEjecucionMapper.toResponse(pipelineEjecucionRepository.save(ejecucion));
    }

    @Transactional
    public PipelineEjecucionResponse actualizar(Long id, PipelineEjecucionRequest request) {
        PipelineEjecucion ejecucion = buscarPorId(id);
        Pipeline pipeline = buscarPipeline(request.pipelineId());
        SolicitudCompra solicitudCompra = buscarSolicitudOpcional(request.solicitudCompraId());
        PipelineEjecucionMapper.updateEntity(ejecucion, request, pipeline, solicitudCompra);
        return PipelineEjecucionMapper.toResponse(pipelineEjecucionRepository.save(ejecucion));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pipelineEjecucionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ejecucion de pipeline no encontrada: " + id);
        }
        pipelineEjecucionRepository.deleteById(id);
    }

    private PipelineEjecucion buscarPorId(Long id) {
        return pipelineEjecucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejecucion de pipeline no encontrada: " + id));
    }

    private Pipeline buscarPipeline(Long pipelineId) {
        return pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline no encontrado: " + pipelineId));
    }

    private SolicitudCompra buscarSolicitudOpcional(Long solicitudCompraId) {
        if (solicitudCompraId == null) {
            return null;
        }
        return solicitudCompraRepository.findById(solicitudCompraId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de compra no encontrada: " + solicitudCompraId));
    }
}
