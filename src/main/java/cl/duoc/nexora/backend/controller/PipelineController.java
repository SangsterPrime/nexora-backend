package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.PipelineRequest;
import cl.duoc.nexora.backend.dto.response.PipelineResponse;
import cl.duoc.nexora.backend.service.PipelineService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping
    public List<PipelineResponse> listar() {
        return pipelineService.listar();
    }

    @GetMapping("/{id}")
    public PipelineResponse obtenerPorId(@PathVariable Long id) {
        return pipelineService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<PipelineResponse> crear(@Valid @RequestBody PipelineRequest request) {
        PipelineResponse response = pipelineService.crear(request);
        return ResponseEntity.created(URI.create("/api/pipelines/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public PipelineResponse actualizar(@PathVariable Long id, @Valid @RequestBody PipelineRequest request) {
        return pipelineService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pipelineService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
