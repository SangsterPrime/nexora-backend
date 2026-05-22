package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.dto.request.ProveedorRequest;
import cl.duoc.nexora.backend.dto.response.ProveedorResponse;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.repository.ProveedorRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void crearProveedorCorrectamente() {
        ProveedorRequest request = proveedorRequest();
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(1L);
            return proveedor;
        });

        ProveedorResponse response = proveedorService.crear(request);

        assertEquals(1L, response.id());
        assertEquals("Proveedor SpA", response.razonSocial());
        assertEquals("proveedor@nexora.cl", response.email());
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    void listarProveedores() {
        Pageable pageable = PageRequest.of(0, 20);
        Proveedor proveedor = proveedor(1L);
        when(proveedorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(proveedor), pageable, 1));

        Page<ProveedorResponse> response = proveedorService.listar(null, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("Proveedor SpA", response.getContent().getFirst().razonSocial());
    }

    @Test
    void obtenerProveedorPorIdExistente() {
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L)));

        ProveedorResponse response = proveedorService.obtenerPorId(1L);

        assertEquals(1L, response.id());
        assertEquals("76000000-1", response.rut());
    }

    @Test
    void obtenerProveedorPorIdInexistenteLanzaResourceNotFoundException() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> proveedorService.obtenerPorId(99L));
    }

    private ProveedorRequest proveedorRequest() {
        return new ProveedorRequest(
                "76000000-1",
                "Proveedor SpA",
                "Contacto Uno",
                "proveedor@nexora.cl",
                "+56911111111",
                "Av. Siempre Viva 123",
                BigDecimal.valueOf(95),
                BigDecimal.valueOf(90),
                EstadoProveedor.ACTIVO
        );
    }

    private Proveedor proveedor(Long id) {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setRut("76000000-1");
        proveedor.setRazonSocial("Proveedor SpA");
        proveedor.setNombreContacto("Contacto Uno");
        proveedor.setEmail("proveedor@nexora.cl");
        proveedor.setTelefono("+56911111111");
        proveedor.setDireccion("Av. Siempre Viva 123");
        proveedor.setReputacionScore(BigDecimal.valueOf(95));
        proveedor.setCumplimientoScore(BigDecimal.valueOf(90));
        proveedor.setEstado(EstadoProveedor.ACTIVO);
        return proveedor;
    }
}
