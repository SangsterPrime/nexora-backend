package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de la petición {@code POST /api/ml/score}. Se reenvía tal cual al
 * servicio Python {@code POST /score}.
 *
 * <p>Estructura flexible para no acoplar el backend a un esquema de features
 * específico del modelo.</p>
 *
 * @param registros  Lista de registros a scorear, cada uno como mapa clave/valor (opcional).
 * @param parametros Opciones adicionales de scoring (opcional).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MlScoreRequest(
        List<Map<String, Object>> registros,
        Map<String, Object> parametros
) {
}
