package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.SolicitudCompraRequest;
import cl.duoc.nexora.backend.dto.response.SolicitudCompraResponse;
import cl.duoc.nexora.backend.service.SolicitudCompraService;
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
@RequestMapping("/api/solicitudes-compra")
@RequiredArgsConstructor
public class SolicitudCompraController {

    private final SolicitudCompraService solicitudCompraService;

    @GetMapping
    public List<SolicitudCompraResponse> listar() {
        return solicitudCompraService.listar();
    }

    @GetMapping("/{id}")
    public SolicitudCompraResponse obtenerPorId(@PathVariable Long id) {
        return solicitudCompraService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<SolicitudCompraResponse> crear(@Valid @RequestBody SolicitudCompraRequest request) {
        SolicitudCompraResponse response = solicitudCompraService.crear(request);
        return ResponseEntity.created(URI.create("/api/solicitudes-compra/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public SolicitudCompraResponse actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudCompraRequest request) {
        return solicitudCompraService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        solicitudCompraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
