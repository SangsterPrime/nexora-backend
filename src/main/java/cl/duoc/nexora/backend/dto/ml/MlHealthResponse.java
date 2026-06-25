package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del endpoint {@code GET /health} del servicio Python de IA.
 *
 * <p>Tolerante a campos extra ({@code ignoreUnknown}) para no romperse si el
 * servicio agrega información adicional.</p>
 *
 * @param status        Estado reportado por el servicio (p. ej. {@code "ok"}).
 * @param service       Nombre del servicio Python.
 * @param version       Versión del servicio o del modelo (opcional).
 * @param modeloCargado {@code true} si hay un modelo entrenado cargado en memoria (opcional).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlHealthResponse(
        String status,
        String service,
        String version,
        @JsonProperty("model_loaded") Boolean modeloCargado
) {
}
