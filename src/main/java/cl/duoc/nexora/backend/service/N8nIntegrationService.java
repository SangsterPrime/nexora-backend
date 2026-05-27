package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.config.N8nProperties;
import cl.duoc.nexora.backend.dto.integration.N8nEventRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Servicio responsable de enviar eventos a n8n mediante su webhook.
 *
 * <p>La integración es <strong>opcional y no bloqueante</strong>: si n8n está
 * desactivado ({@code N8N_ENABLED=false}) o si el webhook falla, el flujo
 * principal de Nexora no se interrumpe.</p>
 *
 * <p>El secret nunca se imprime en los logs.</p>
 */
@Service
@Slf4j
public class N8nIntegrationService {

    private final N8nProperties n8nProperties;
    private final RestClient restClient;

    /**
     * Constructor principal usado por Spring Boot.
     * Crea un {@link RestClient} simple con configuración por defecto.
     */
    @Autowired
    public N8nIntegrationService(N8nProperties n8nProperties) {
        this.n8nProperties = n8nProperties;
        this.restClient = RestClient.create();
    }

    /**
     * Constructor de paquete reservado para tests unitarios.
     * Permite inyectar un {@link RestClient} ya construido (p.ej. con interceptor de fallo).
     */
    N8nIntegrationService(N8nProperties n8nProperties, RestClient restClient) {
        this.n8nProperties = n8nProperties;
        this.restClient = restClient;
    }

    // ── API pública ────────────────────────────────────────────────────────────

    /** @return {@code true} si la integración n8n está habilitada. */
    public boolean isEnabled() {
        return n8nProperties.isEnabled();
    }

    /**
     * Envía un evento al webhook de n8n.
     *
     * <ul>
     *   <li>Si la integración está desactivada, retorna inmediatamente sin llamar a n8n.</li>
     *   <li>Si {@code webhookUrl} está vacía, retorna error controlado.</li>
     *   <li>Si n8n falla (error HTTP o conexión), registra un warning y retorna
     *       {@code enviado=false} sin propagar la excepción.</li>
     * </ul>
     *
     * @param event Datos del evento a enviar.
     * @return Resultado del despacho.
     */
    public N8nDispatchResult enviarEvento(N8nEventRequest event) {

        if (!n8nProperties.isEnabled()) {
            log.debug("Integración n8n desactivada — omitiendo evento: {}", event.getEvento());
            return new N8nDispatchResult(false, "Integración n8n desactivada");
        }

        String webhookUrl = n8nProperties.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("N8N_WEBHOOK_URL no configurada — no se puede enviar evento '{}'", event.getEvento());
            return new N8nDispatchResult(false, "N8N_WEBHOOK_URL no configurada");
        }

        try {
            restClient.post()
                    .uri(webhookUrl)
                    .header("X-NEXORA-WEBHOOK-SECRET", n8nProperties.getSecret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Evento n8n enviado — evento={}, entidad={}, id={}",
                    event.getEvento(), event.getEntidad(), event.getEntidadId());
            return new N8nDispatchResult(true, "Evento enviado a n8n correctamente");

        } catch (RestClientException e) {
            log.warn("Error HTTP al enviar evento '{}' a n8n: {} — flujo principal no interrumpido",
                    event.getEvento(), e.getMessage());
            return new N8nDispatchResult(false, "Error al comunicarse con n8n: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Error inesperado al enviar evento '{}' a n8n: {} — flujo principal no interrumpido",
                    event.getEvento(), e.getMessage());
            return new N8nDispatchResult(false, "Error inesperado al comunicarse con n8n: " + e.getMessage());
        }
    }

    // ── DTO interno ────────────────────────────────────────────────────────────

    /**
     * Resultado del intento de despacho de un evento a n8n.
     *
     * @param enviado {@code true} si n8n respondió exitosamente.
     * @param mensaje Descripción del resultado o del error.
     */
    public record N8nDispatchResult(boolean enviado, String mensaje) {}
}
