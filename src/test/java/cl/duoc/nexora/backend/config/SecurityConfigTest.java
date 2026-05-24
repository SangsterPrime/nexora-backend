package cl.duoc.nexora.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthPermanecePublico() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void dbTestPermanecePublicoParaPrueba() throws Exception {
        mockMvc.perform(get("/db-test"))
                .andExpect(status().isOk());
    }

    @Test
    void proveedoresRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isUnauthorized());
    }
}
