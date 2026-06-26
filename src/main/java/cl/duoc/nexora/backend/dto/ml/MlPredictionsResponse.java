package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Respuesta con resultados scoreados. La usan tanto {@code POST /score} (scoring
 * inmediato) como {@code GET /predictions} (predicciones almacenadas).
 *
 * @param status       Estado reportado por el servicio (opcional).
 * @param total        Cantidad de registros scoreados (opcional).
 * @param predicciones Lista de predicciones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlPredictionsResponse(
        String status,
        Integer total,
        List<MlPrediction> predicciones
) {
}
