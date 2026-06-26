package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.PipelineRequest;
import cl.duoc.nexora.backend.dto.response.PipelineResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.PipelineMapper;
import cl.duoc.nexora.backend.model.Pipeline;
import cl.duoc.nexora.backend.repository.PipelineRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final PipelineRepository pipelineRepository;

    @Transactional(readOnly = true)
    public List<PipelineResponse> listar() {
        return pipelineRepository.findAll().stream()
                .map(PipelineMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PipelineResponse obtenerPorId(Long id) {
        return PipelineMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public PipelineResponse crear(PipelineRequest request) {
        Pipeline pipeline = PipelineMapper.toEntity(request);
        return PipelineMapper.toResponse(pipelineRepository.save(pipeline));
    }

    @Transactional
    public PipelineResponse actualizar(Long id, PipelineRequest request) {
        Pipeline pipeline = buscarPorId(id);
        PipelineMapper.updateEntity(pipeline, request);
        return PipelineMapper.toResponse(pipelineRepository.save(pipeline));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pipelineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pipeline no encontrado: " + id);
        }
        pipelineRepository.deleteById(id);
    }

    private Pipeline buscarPorId(Long id) {
        return pipelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline no encontrado: " + id));
    }
}
