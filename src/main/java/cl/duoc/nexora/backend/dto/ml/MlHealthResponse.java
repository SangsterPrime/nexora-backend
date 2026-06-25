package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Respuesta del endpoint {@code GET /api/ml/health}.
 *
 * <p>En modo {@code API} los primeros campos provienen del servicio Python
 * (tolerante a campos extra con {@code ignoreUnknown}). En modo {@code CRON} el
 * backend completa el estado a partir de los datos en Neon.</p>
 *
 * @param status              Estado general (p. ej. {@code "ok"}, {@code "sin_datos"}).
 * @param service             Nombre del servicio que responde.
 * @param version             Versión del servicio o del modelo (opcional).
 * @param modeloCargado       {@code true} si hay un modelo cargado en memoria (solo modo API, opcional).
 * @param modo                Modo activo de la integración: {@code "API"} o {@code "CRON"}.
 * @param metricasDisponibles {@code true} si existen métricas registradas (modo CRON).
 * @param ultimaEjecucion     Fecha/hora de la última corrida registrada (modo CRON).
 * @param totalPredicciones   Cantidad de predicciones almacenadas (modo CRON).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlHealthResponse(
        String status,
        String service,
        String version,
        @JsonProperty("model_loaded") Boolean modeloCargado,
        String modo,
        Boolean metricasDisponibles,
        LocalDateTime ultimaEjecucion,
        Long totalPredicciones
) {
}
