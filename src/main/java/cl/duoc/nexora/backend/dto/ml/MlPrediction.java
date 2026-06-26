package cl.duoc.nexora.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Un registro scoreado por el modelo de IA.
 *
 * <p>Estructura flexible: todos los campos son opcionales para tolerar distintas
 * formas de salida del servicio Python.</p>
 *
 * @param id          Identificador del registro en el servicio ML (opcional).
 * @param entidad     Tipo de entidad evaluada (p. ej. {@code "COTIZACION"}, opcional).
 * @param entidadId   ID de la entidad de negocio asociada (opcional).
 * @param score       Score numérico crudo (opcional).
 * @param probabilidad Probabilidad asociada a la clase positiva (opcional).
 * @param prediccion  Etiqueta o clase predicha (opcional).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlPrediction(
        String id,
        String entidad,
        @JsonProperty("entidad_id") Long entidadId,
        Double score,
        Double probabilidad,
        String prediccion,
        // Campos ricos de predicciones.json / clientes_scoreados
        Integer edad,
        @JsonProperty("anos_cliente") Integer anosCliente,
        @JsonProperty("uso_datos_gb") Double usoDatosGb,
        @JsonProperty("llamadas_mes") Integer llamadasMes,
        Integer reclamos,
        @JsonProperty("plan_premium") Integer planPremium,
        Integer abandona,
        @JsonProperty("prob_abandono") Double probAbandono,
        @JsonProperty("segmento_riesgo") String segmentoRiesgo,
        @JsonProperty("accion_retencion") String accionRetencion
) {
}
