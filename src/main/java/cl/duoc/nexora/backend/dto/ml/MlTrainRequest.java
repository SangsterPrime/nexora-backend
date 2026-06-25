package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Cuerpo de la petición {@code POST /api/ml/train}. Se reenvía tal cual al
 * servicio Python {@code POST /train}.
 *
 * <p>Todos los campos son opcionales: el frontend puede disparar un
 * entrenamiento con cuerpo vacío y dejar que el servicio use sus valores por
 * defecto.</p>
 *
 * @param dataset     Identificador o nombre del dataset a entrenar (opcional).
 * @param parametros  Hiperparámetros u opciones adicionales para el entrenamiento (opcional).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MlTrainRequest(
        String dataset,
        Map<String, Object> parametros
) {
}
