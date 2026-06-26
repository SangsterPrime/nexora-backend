package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.integration.N8nEventRequest;
import cl.duoc.nexora.backend.service.N8nIntegrationService;
import cl.duoc.nexora.backend.service.N8nIntegrationService.N8nDispatchResult;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de administración para verificar las integraciones externas de Nexora.
 *
 * <p>Ruta base: {@code /api/integrations}</p>
 * <p>Requiere autenticación. No expone secretos en la respuesta.</p>
 */
@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final N8nIntegrationService n8nIntegrationService;

    /**
     * Verifica la conectividad con el webhook de n8n enviando un evento de prueba.
     *
     * <p>Respuestas posibles:</p>
     * <ul>
     *   <li>{@code {"ok": false, "message": "Integración n8n desactivada"}} — N8N_ENABLED=false</li>
     *   <li>{@code {"ok": false, "message": "N8N_WEBHOOK_URL no configurada"}} — URL vacía</li>
     *   <li>{@code {"ok": true,  "message": "Evento de prueba enviado a n8n"}} — OK</li>
     *   <li>{@code {"ok": false, "message": "Error al comunicarse con n8n: ..."}} — fallo</li>
     * </ul>
     */
    @PostMapping("/n8n/test")
    public ResponseEntity<TestResponse> testN8n() {

        N8nEventRequest testEvent = N8nEventRequest.builder()
                .evento("NEXORA_TEST")
                .entidad("INTEGRACION")
                .accion("TEST")
                .payload(Map.of("mensaje", "Prueba de conexión desde NEXORA hacia n8n"))
                .timestamp(LocalDateTime.now())
                .build();

        N8nDispatchResult result = n8nIntegrationService.enviarEvento(testEvent);

        String mensaje = result.enviado()
                ? "Evento de prueba enviado a n8n"
                : result.mensaje();

        return ResponseEntity.ok(new TestResponse(result.enviado(), mensaje));
    }

    // ── DTO de respuesta ───────────────────────────────────────────────────────

    /**
     * Respuesta del endpoint de prueba de integración.
     *
     * @param ok      {@code true} si el evento fue recibido por n8n.
     * @param message Descripción del resultado.
     */
    public record TestResponse(boolean ok, String message) {}
}
