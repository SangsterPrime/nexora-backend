package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.request.ProveedorRequest;
import cl.duoc.nexora.backend.dto.response.ProveedorResponse;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.service.ProveedorService;
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
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public Page<ProveedorResponse> listar(
            @RequestParam(required = false) EstadoProveedor estado,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return proveedorService.listar(estado, pageable);
    }

    @GetMapping("/{id}")
    public ProveedorResponse obtenerPorId(@PathVariable Long id) {
        return proveedorService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = proveedorService.crear(request);
        return ResponseEntity.created(URI.create("/api/proveedores/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ProveedorResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
        return proveedorService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
