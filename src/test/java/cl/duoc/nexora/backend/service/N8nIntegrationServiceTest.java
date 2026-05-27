package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.config.N8nProperties;
import cl.duoc.nexora.backend.dto.integration.N8nEventRequest;
import cl.duoc.nexora.backend.service.N8nIntegrationService.N8nDispatchResult;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class N8nIntegrationServiceTest {

    @Mock
    private N8nProperties n8nProperties;

    // ── Caso 1: integración desactivada ────────────────────────────────────────

    @Test
    void enviarEvento_cuandoDesactivado_retornaNoEnviadoSinLlamarAl() {
        when(n8nProperties.isEnabled()).thenReturn(false);
        // RestClient.create() nunca será invocado porque el guard devuelve antes
        N8nIntegrationService service = new N8nIntegrationService(n8nProperties, RestClient.create());

        N8nDispatchResult result = service.enviarEvento(buildTestEvent());

        assertFalse(result.enviado());
        assertEquals("Integración n8n desactivada", result.mensaje());
    }

    // ── Caso 2: URL vacía ──────────────────────────────────────────────────────

    @Test
    void enviarEvento_cuandoUrlVacia_retornaNoEnviado() {
        when(n8nProperties.isEnabled()).thenReturn(true);
        when(n8nProperties.getWebhookUrl()).thenReturn("");
        N8nIntegrationService service = new N8nIntegrationService(n8nProperties, RestClient.create());

        N8nDispatchResult result = service.enviarEvento(buildTestEvent());

        assertFalse(result.enviado());
        assertEquals("N8N_WEBHOOK_URL no configurada", result.mensaje());
    }

    // ── Caso 3: n8n caído, no debe romper el flujo ─────────────────────────────

    @Test
    void enviarEvento_cuandoWebhookFalla_noLanzaExcepcionYRetornaNoEnviado() {
        when(n8nProperties.isEnabled()).thenReturn(true);
        when(n8nProperties.getWebhookUrl()).thenReturn("http://n8n-inexistente.local/webhook");
        when(n8nProperties.getSecret()).thenReturn("test-secret");

        // Construye un RestClient con interceptor que simula fallo de conexión
        RestClient failingClient = RestClient.builder()
                .requestInterceptors(interceptors ->
                        interceptors.add((request, body, execution) -> {
                            throw new IOException("Conexión rechazada simulada");
                        }))
                .build();

        N8nIntegrationService service = new N8nIntegrationService(n8nProperties, failingClient);

        N8nDispatchResult result = assertDoesNotThrow(() -> service.enviarEvento(buildTestEvent()));

        assertFalse(result.enviado());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private N8nEventRequest buildTestEvent() {
        return N8nEventRequest.builder()
                .evento("NEXORA_TEST")
                .entidad("INTEGRACION")
                .accion("TEST")
                .payload(Map.of("mensaje", "test"))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
