package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.CotizacionRequest;
import cl.duoc.nexora.backend.dto.response.CotizacionResponse;
import cl.duoc.nexora.backend.service.CotizacionService;
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
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @GetMapping
    public List<CotizacionResponse> listar() {
        return cotizacionService.listar();
    }

    @GetMapping("/{id}")
    public CotizacionResponse obtenerPorId(@PathVariable Long id) {
        return cotizacionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> crear(@Valid @RequestBody CotizacionRequest request) {
        CotizacionResponse response = cotizacionService.crear(request);
        return ResponseEntity.created(URI.create("/api/cotizaciones/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public CotizacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CotizacionRequest request) {
        return cotizacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cotizacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
