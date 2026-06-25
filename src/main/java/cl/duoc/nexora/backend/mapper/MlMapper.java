package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlPrediction;
import cl.duoc.nexora.backend.model.MlMetrica;
import cl.duoc.nexora.backend.model.MlPrediccion;
import java.util.List;

/**
 * Conversión entre las entidades del modo CRON ({@link MlMetrica}, {@link MlPrediccion})
 * y los DTOs de respuesta.
 *
 * <p>La matriz de confusión se entrega ya parseada (el parseo del JSON vive en el
 * service, que dispone del {@code ObjectMapper}).</p>
 */
public final class MlMapper {

    private MlMapper() {
    }

    public static MlMetricsResponse toMetricsResponse(MlMetrica metrica, List<List<Integer>> matrizConfusion) {
        return new MlMetricsResponse(
                metrica.getAccuracy(),
                metrica.getRecall(),
                metrica.getPrecision(),
                metrica.getF1(),
                metrica.getRocAuc(),
                metrica.getGini(),
                matrizConfusion
        );
    }

    public static MlPrediction toPrediction(MlPrediccion prediccion) {
        return new MlPrediction(
                prediccion.getId() != null ? String.valueOf(prediccion.getId()) : null,
                prediccion.getEntidad(),
                prediccion.getEntidadId(),
                prediccion.getScore(),
                prediccion.getProbabilidad(),
                prediccion.getPrediccion()
        );
    }
}
