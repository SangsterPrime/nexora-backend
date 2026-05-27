package cl.duoc.nexora.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para la integración con n8n.
 *
 * <p>Valores configurables via variables de entorno en Render:</p>
 * <ul>
 *   <li>{@code N8N_ENABLED} — activa o desactiva la integración (default: false)</li>
 *   <li>{@code N8N_WEBHOOK_URL} — URL del webhook de producción en n8n</li>
 *   <li>{@code N8N_WEBHOOK_SECRET} — header de autenticación X-NEXORA-WEBHOOK-SECRET</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "nexora.integrations.n8n")
@Getter
@Setter
public class N8nProperties {

    /** Activa o desactiva el envío de eventos a n8n. Default: {@code false}. */
    private boolean enabled;

    /** URL completa del webhook de n8n (Production URL, no test). */
    private String webhookUrl = "";

    /** Valor del header {@code X-NEXORA-WEBHOOK-SECRET}. No se imprime en logs. */
    private String secret = "";
}
