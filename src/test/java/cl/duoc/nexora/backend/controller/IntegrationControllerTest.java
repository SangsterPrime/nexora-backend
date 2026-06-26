package cl.duoc.nexora.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.nexora.backend.config.SecurityConfig;
import cl.duoc.nexora.backend.dto.integration.N8nEventRequest;
import cl.duoc.nexora.backend.service.N8nIntegrationService;
import cl.duoc.nexora.backend.service.N8nIntegrationService.N8nDispatchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = IntegrationController.class,
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class},
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private N8nIntegrationService n8nIntegrationService;

    // ── Caso: integración desactivada ──────────────────────────────────────────

    @Test
    void testN8n_cuandoDesactivado_retornaOkFalse() throws Exception {
        when(n8nIntegrationService.enviarEvento(any(N8nEventRequest.class)))
                .thenReturn(new N8nDispatchResult(false, "Integración n8n desactivada"));

        mockMvc.perform(post("/api/integrations/n8n/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("Integración n8n desactivada"));
    }

    // ── Caso: integración activada y evento enviado ────────────────────────────

    @Test
    void testN8n_cuandoActivadoYExitoso_retornaOkTrue() throws Exception {
        when(n8nIntegrationService.enviarEvento(any(N8nEventRequest.class)))
                .thenReturn(new N8nDispatchResult(true, "Evento enviado a n8n correctamente"));

        mockMvc.perform(post("/api/integrations/n8n/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("Evento de prueba enviado a n8n"));
    }

    // ── Caso: URL no configurada ────────────────────────────────────────────────

    @Test
    void testN8n_cuandoUrlNoConfigurada_retornaOkFalse() throws Exception {
        when(n8nIntegrationService.enviarEvento(any(N8nEventRequest.class)))
                .thenReturn(new N8nDispatchResult(false, "N8N_WEBHOOK_URL no configurada"));

        mockMvc.perform(post("/api/integrations/n8n/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("N8N_WEBHOOK_URL no configurada"));
    }
}
