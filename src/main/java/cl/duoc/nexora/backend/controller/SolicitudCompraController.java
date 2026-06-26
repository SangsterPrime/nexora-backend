package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.SolicitudCompraRequest;
import cl.duoc.nexora.backend.dto.response.SolicitudCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.service.SolicitudCompraService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes-compra")
@RequiredArgsConstructor
public class SolicitudCompraController {

    private final SolicitudCompraService solicitudCompraService;

    @GetMapping
    public Page<SolicitudCompraResponse> listar(
            @RequestParam(required = false) EstadoSolicitudCompra estado,
            @RequestParam(required = false) Long usuarioSolicitanteId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return solicitudCompraService.listar(estado, usuarioSolicitanteId, pageable);
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
