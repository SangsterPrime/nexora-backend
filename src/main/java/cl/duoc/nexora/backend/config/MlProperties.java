package cl.duoc.nexora.backend.config;

import cl.duoc.nexora.backend.enums.MlMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para la integración con el servicio de IA/DataOps
 * (proyecto {@code EntrenamientoAI}).
 *
 * <p>Hay dos modos de despliegue (ver {@link MlMode}):</p>
 * <ul>
 *   <li>{@code API} — el backend habla por HTTP con un servicio Python (FastAPI)
 *       que expone {@code /health}, {@code /train}, {@code /score}, {@code /metrics}
 *       y {@code /predictions}.</li>
 *   <li>{@code CRON} — el entrenamiento corre como Render Cron Job que escribe en
 *       Neon; el backend lee {@code ml_metricas} y {@code ml_predicciones} desde la
 *       base de datos y no requiere un servicio HTTP activo.</li>
 * </ul>
 *
 * <p>El frontend nunca llama al servicio Python: todo pasa por Spring Boot bajo
 * {@code /api/ml/**}.</p>
 *
 * <p>Valores configurables via variables de entorno en Render:</p>
 * <ul>
 *   <li>{@code NEXORA_ML_ENABLED} — activa o desactiva la integración (default: false)</li>
 *   <li>{@code NEXORA_ML_MODE} — {@code API} o {@code CRON} (default: API)</li>
 *   <li>{@code NEXORA_ML_URL} — URL base del servicio Python (solo modo API)</li>
 *   <li>{@code NEXORA_ML_API_KEY} — API key enviada en el header {@code X-API-Key}. No se imprime en logs.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "nexora.integrations.ml")
@Getter
@Setter
public class MlProperties {

    /** Activa o desactiva la integración ML. Default: {@code false}. */
    private boolean enabled;

    /** Modo de operación: {@link MlMode#API} (HTTP) o {@link MlMode#CRON} (lectura desde Neon). Default: {@code API}. */
    private MlMode mode = MlMode.API;

    /** URL base del servicio Python, sin slash final obligatorio (solo modo {@code API}). */
    private String url = "";

    /** Valor del header {@code X-API-Key}. Secreto: no se imprime en logs ni se expone al frontend. */
    private String apiKey = "";
}
