package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Métricas del modelo entrenado, devueltas por {@code GET /metrics} y embebidas
 * en la respuesta de {@code POST /train}.
 *
 * <p>Los nombres en {@code snake_case} del servicio Python ({@code roc_auc},
 * {@code matriz_confusion}) se mapean a {@code camelCase} en Java.</p>
 *
 * @param accuracy        Exactitud global.
 * @param recall          Sensibilidad / exhaustividad.
 * @param precision       Precisión.
 * @param f1              F1-score.
 * @param rocAuc          Área bajo la curva ROC.
 * @param gini            Coeficiente de Gini.
 * @param matrizConfusion Matriz de confusión como lista de filas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlMetricsResponse(
        Double accuracy,
        Double recall,
        Double precision,
        Double f1,
        @JsonProperty("roc_auc") Double rocAuc,
        Double gini,
        @JsonProperty("matriz_confusion") List<List<Integer>> matrizConfusion
) {
}
