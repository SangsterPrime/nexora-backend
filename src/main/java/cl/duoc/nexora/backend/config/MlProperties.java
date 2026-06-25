package cl.duoc.nexora.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para la integración con el servicio Python de
 * IA/DataOps (proyecto {@code EntrenamientoAI}).
 *
 * <p>El servicio Python expone {@code /health}, {@code /train}, {@code /score},
 * {@code /metrics} y {@code /predictions}. El frontend nunca lo llama directo:
 * todo pasa por Spring Boot bajo {@code /api/ml/**}.</p>
 *
 * <p>Valores configurables via variables de entorno en Render:</p>
 * <ul>
 *   <li>{@code NEXORA_ML_ENABLED} — activa o desactiva la integración (default: false)</li>
 *   <li>{@code NEXORA_ML_URL} — URL base del servicio Python, p. ej. {@code https://nexora-ml.onrender.com}</li>
 *   <li>{@code NEXORA_ML_API_KEY} — API key enviada en el header {@code X-API-Key}. No se imprime en logs.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "nexora.integrations.ml")
@Getter
@Setter
public class MlProperties {

    /** Activa o desactiva las llamadas al servicio ML. Default: {@code false}. */
    private boolean enabled;

    /** URL base del servicio Python (sin slash final obligatorio). */
    private String url = "";

    /** Valor del header {@code X-API-Key}. Secreto: no se imprime en logs ni se expone al frontend. */
    private String apiKey = "";
}
