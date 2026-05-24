package cl.duoc.nexora.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.nexora.backend.dto.request.ProveedorRequest;
import cl.duoc.nexora.backend.dto.response.ProveedorResponse;
import cl.duoc.nexora.backend.config.SecurityConfig;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.service.ProveedorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = ProveedorController.class,
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class},
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProveedorService proveedorService;

    @Test
    void listarProveedoresDevuelveOk() throws Exception {
        when(proveedorService.listar(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(proveedorResponse())));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("proveedor@nexora.cl"));
    }

    @Test
    void crearProveedorConPayloadValidoDevuelveCreated() throws Exception {
        when(proveedorService.crear(any(ProveedorRequest.class))).thenReturn(proveedorResponse());

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/proveedores/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.razonSocial").value("Proveedor SpA"));
    }

    @Test
    void crearProveedorConPayloadInvalidoDevuelveBadRequest() throws Exception {
        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
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

    private ProveedorResponse proveedorResponse() {
        return new ProveedorResponse(
                1L,
                "76000000-1",
                "Proveedor SpA",
                "Contacto Uno",
                "proveedor@nexora.cl",
                "+56911111111",
                "Av. Siempre Viva 123",
                BigDecimal.valueOf(95),
                BigDecimal.valueOf(90),
                EstadoProveedor.ACTIVO,
                null
        );
    }
}
