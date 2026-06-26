package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.NegociacionRequest;
import cl.duoc.nexora.backend.dto.response.NegociacionResponse;
import cl.duoc.nexora.backend.service.NegociacionService;
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
@RequestMapping("/api/negociaciones")
@RequiredArgsConstructor
public class NegociacionController {

    private final NegociacionService negociacionService;

    @GetMapping
    public List<NegociacionResponse> listar() {
        return negociacionService.listar();
    }

    @GetMapping("/{id}")
    public NegociacionResponse obtenerPorId(@PathVariable Long id) {
        return negociacionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<NegociacionResponse> crear(@Valid @RequestBody NegociacionRequest request) {
        NegociacionResponse response = negociacionService.crear(request);
        return ResponseEntity.created(URI.create("/api/negociaciones/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public NegociacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody NegociacionRequest request) {
        return negociacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        negociacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
