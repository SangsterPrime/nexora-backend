package cl.duoc.nexora.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.nexora.backend.config.SecurityConfig;
import cl.duoc.nexora.backend.dto.ml.MlHealthResponse;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.exception.MlServiceException;
import cl.duoc.nexora.backend.service.MlService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = MlController.class,
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class},
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class MlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MlService mlService;

    // ── Health OK ──────────────────────────────────────────────────────────────

    @Test
    void health_devuelveOk() throws Exception {
        when(mlService.health())
                .thenReturn(new MlHealthResponse("ok", "entrenamiento-ai", "1.0", true, "API", null, null, null));

        mockMvc.perform(get("/api/ml/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("entrenamiento-ai"));
    }

    // ── Métricas: mapeo snake_case en la salida ────────────────────────────────

    @Test
    void metrics_devuelveMetricasConNombresSnakeCase() throws Exception {
        when(mlService.metrics()).thenReturn(new MlMetricsResponse(
                0.95, 0.90, 0.92, 0.91, 0.97, 0.94, List.of(List.of(10, 2), List.of(1, 20))));

        mockMvc.perform(get("/api/ml/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accuracy").value(0.95))
                .andExpect(jsonPath("$.roc_auc").value(0.97))
                .andExpect(jsonPath("$.matriz_confusion[0][0]").value(10));
    }

    // ── Entrenamiento dispara el servicio ──────────────────────────────────────

    @Test
    void train_devuelveOk() throws Exception {
        when(mlService.train(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new cl.duoc.nexora.backend.dto.ml.MlTrainResponse("ok", "listo", 10, 5L, null));

        mockMvc.perform(post("/api/ml/train")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    // ── Error del servicio ML se traduce a 502 con mensaje claro ───────────────

    @Test
    void health_cuandoServicioFalla_devuelve502ConMensaje() throws Exception {
        when(mlService.health())
                .thenThrow(new MlServiceException(HttpStatus.BAD_GATEWAY, "No se pudo conectar con el servicio de IA"));

        mockMvc.perform(get("/api/ml/health"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.mensaje").value("No se pudo conectar con el servicio de IA"));
    }

    // ── Integración desactivada se traduce a 503 ───────────────────────────────

    @Test
    void predictions_cuandoDesactivado_devuelve503() throws Exception {
        when(mlService.predictions())
                .thenThrow(new MlServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Integración ML desactivada. Configure NEXORA_ML_ENABLED=true para habilitarla."));

        mockMvc.perform(get("/api/ml/predictions"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
