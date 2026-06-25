package cl.duoc.nexora.backend.enums;

/**
 * Modo de operación de la integración con el servicio de IA (EntrenamientoAI).
 *
 * <ul>
 *   <li>{@link #API} — el backend habla por HTTP con un servicio Python (FastAPI) activo.</li>
 *   <li>{@link #CRON} — el entrenamiento corre como Render Cron Job que escribe en Neon
 *       (tablas {@code ml_metricas} y {@code ml_predicciones}); el backend solo lee de la base
 *       y no depende de un servicio HTTP activo.</li>
 * </ul>
 */
public enum MlMode {
    API,
    CRON
}
