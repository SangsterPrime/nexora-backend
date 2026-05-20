package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.PipelineEjecucionRequest;
import cl.duoc.nexora.backend.dto.response.PipelineEjecucionResponse;
import cl.duoc.nexora.backend.service.PipelineEjecucionService;
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
@RequestMapping("/api/pipeline-ejecuciones")
@RequiredArgsConstructor
public class PipelineEjecucionController {

    private final PipelineEjecucionService pipelineEjecucionService;

    @GetMapping
    public List<PipelineEjecucionResponse> listar() {
        return pipelineEjecucionService.listar();
    }

    @GetMapping("/{id}")
    public PipelineEjecucionResponse obtenerPorId(@PathVariable Long id) {
        return pipelineEjecucionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<PipelineEjecucionResponse> crear(@Valid @RequestBody PipelineEjecucionRequest request) {
        PipelineEjecucionResponse response = pipelineEjecucionService.crear(request);
        return ResponseEntity.created(URI.create("/api/pipeline-ejecuciones/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public PipelineEjecucionResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PipelineEjecucionRequest request
    ) {
        return pipelineEjecucionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pipelineEjecucionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
