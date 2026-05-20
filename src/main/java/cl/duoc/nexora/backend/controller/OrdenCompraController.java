package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.OrdenCompraRequest;
import cl.duoc.nexora.backend.dto.response.OrdenCompraResponse;
import cl.duoc.nexora.backend.service.OrdenCompraService;
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
@RequestMapping("/api/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @GetMapping
    public List<OrdenCompraResponse> listar() {
        return ordenCompraService.listar();
    }

    @GetMapping("/{id}")
    public OrdenCompraResponse obtenerPorId(@PathVariable Long id) {
        return ordenCompraService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> crear(@Valid @RequestBody OrdenCompraRequest request) {
        OrdenCompraResponse response = ordenCompraService.crear(request);
        return ResponseEntity.created(URI.create("/api/ordenes-compra/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public OrdenCompraResponse actualizar(@PathVariable Long id, @Valid @RequestBody OrdenCompraRequest request) {
        return ordenCompraService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenCompraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
