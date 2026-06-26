package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del endpoint {@code POST /train} del servicio Python de IA.
 *
 * @param status   Estado del entrenamiento (p. ej. {@code "ok"}).
 * @param mensaje  Mensaje descriptivo ({@code message} en el servicio).
 * @param registros Cantidad de registros usados en el entrenamiento ({@code n_samples}).
 * @param duracionMs Duración del entrenamiento en milisegundos reportada por el servicio ({@code duration_ms}).
 * @param metricas Métricas del modelo recién entrenado ({@code metrics}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlTrainResponse(
        String status,
        @JsonAlias("message") String mensaje,
        @JsonProperty("n_samples") Integer registros,
        @JsonProperty("duration_ms") Long duracionMs,
        @JsonAlias("metrics") MlMetricsResponse metricas
) {
}
