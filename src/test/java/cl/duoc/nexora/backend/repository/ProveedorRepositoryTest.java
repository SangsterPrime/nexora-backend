package cl.duoc.nexora.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.model.Proveedor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ProveedorRepositoryTest {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Test
    void guardarYBuscarPorId() {
        Proveedor proveedor = proveedor("76000000-1", "proveedor@nexora.cl");

        Proveedor guardado = proveedorRepository.saveAndFlush(proveedor);

        assertTrue(proveedorRepository.findById(guardado.getId()).isPresent());
        assertEquals("Proveedor SpA", proveedorRepository.findById(guardado.getId()).orElseThrow().getRazonSocial());
    }

    @Test
    void emailDebeSerUnico() {
        proveedorRepository.saveAndFlush(proveedor("76000000-1", "proveedor@nexora.cl"));

        assertThrows(DataIntegrityViolationException.class, () ->
                proveedorRepository.saveAndFlush(proveedor("76000000-2", "proveedor@nexora.cl"))
        );
    }

    @Test
    void rutDebeSerUnico() {
        proveedorRepository.saveAndFlush(proveedor("76000000-1", "proveedor1@nexora.cl"));

        assertThrows(DataIntegrityViolationException.class, () ->
                proveedorRepository.saveAndFlush(proveedor("76000000-1", "proveedor2@nexora.cl"))
        );
    }

    private Proveedor proveedor(String rut, String email) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRut(rut);
        proveedor.setRazonSocial("Proveedor SpA");
        proveedor.setNombreContacto("Contacto Uno");
        proveedor.setEmail(email);
        proveedor.setTelefono("+56911111111");
        proveedor.setDireccion("Av. Siempre Viva 123");
        proveedor.setEstado(EstadoProveedor.ACTIVO);
        return proveedor;
    }
}
