package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.config.MlProperties;
import cl.duoc.nexora.backend.dto.ml.MlHealthResponse;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlPrediction;
import cl.duoc.nexora.backend.dto.ml.MlPredictionsResponse;
import cl.duoc.nexora.backend.dto.ml.MlScoreRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainResponse;
import cl.duoc.nexora.backend.enums.EstadoPipelineEjecucion;
import cl.duoc.nexora.backend.enums.MlMode;
import cl.duoc.nexora.backend.enums.TipoKpi;
import cl.duoc.nexora.backend.enums.TipoPipeline;
import cl.duoc.nexora.backend.exception.MlServiceException;
import cl.duoc.nexora.backend.mapper.MlMapper;
import cl.duoc.nexora.backend.model.KpiResultado;
import cl.duoc.nexora.backend.model.MlMetrica;
import cl.duoc.nexora.backend.model.MlPrediccion;
import cl.duoc.nexora.backend.model.Pipeline;
import cl.duoc.nexora.backend.model.PipelineEjecucion;
import cl.duoc.nexora.backend.model.PipelineError;
import cl.duoc.nexora.backend.repository.KpiResultadoRepository;
import cl.duoc.nexora.backend.repository.MlMetricaRepository;
import cl.duoc.nexora.backend.repository.MlPrediccionRepository;
import cl.duoc.nexora.backend.repository.PipelineEjecucionRepository;
import cl.duoc.nexora.backend.repository.PipelineErrorRepository;
import cl.duoc.nexora.backend.repository.PipelineRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Orquesta la integración con el servicio de IA ({@code EntrenamientoAI}) según el
 * modo configurado en {@link MlProperties#getMode()}:
 *
 * <ul>
 *   <li><strong>API</strong> — habla por HTTP con el servicio Python; {@code train}
 *       y {@code score} se registran como {@link PipelineEjecucion}, los fallos
 *       generan {@link PipelineError} y las métricas se guardan como {@link KpiResultado}.</li>
 *   <li><strong>CRON</strong> — el entrenamiento corre como Render Cron Job que
 *       escribe en Neon; el backend lee {@code ml_metricas} y {@code ml_predicciones}.
 *       {@code train} responde 409 (lo ejecuta el cron) y {@code score} devuelve las
 *       últimas predicciones disponibles.</li>
 * </ul>
 *
 * <p>En cualquier modo se conserva trazabilidad y se devuelven errores claros al
 * frontend, sin exponer credenciales ni URLs internas.</p>
 *
 * <p>Las escrituras de modo API no son transaccionales a propósito: cada
 * {@code save} confirma de inmediato, de modo que el registro de error sobrevive
 * aunque se re-lance la excepción.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MlService {

    private static final String PIPELINE_ENTRENAMIENTO = "ML - Entrenamiento";
    private static final String PIPELINE_SCORING = "ML - Scoring";

    /** Límite máximo de predicciones devueltas en una sola lectura (modo CRON). */
    private static final int MAX_PREDICCIONES = 100;

    private final MlProperties mlProperties;
    private final MlServiceClient mlServiceClient;
    private final PipelineRepository pipelineRepository;
    private final PipelineEjecucionRepository pipelineEjecucionRepository;
    private final PipelineErrorRepository pipelineErrorRepository;
    private final KpiResultadoRepository kpiResultadoRepository;
    private final MlMetricaRepository mlMetricaRepository;
    private final MlPrediccionRepository mlPrediccionRepository;

    /** Parser dedicado para la matriz de confusión almacenada como JSON; no requiere bean de Spring. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ── API pública ──────────────────────────────────────────────────────────────

    public MlHealthResponse health() {
        verificarHabilitado();
        return esCron() ? healthCron() : healthApi();
    }

    public MlMetricsResponse metrics() {
        verificarHabilitado();
        return esCron() ? metricsCron() : mlServiceClient.metrics();
    }

    public MlPredictionsResponse predictions() {
        verificarHabilitado();
        return esCron() ? leerUltimasPredicciones() : mlServiceClient.predictions();
    }

    public MlTrainResponse train(MlTrainRequest request) {
        verificarHabilitado();
        if (esCron()) {
            // El entrenamiento lo dispara el Render Cron Job; no se llama al servicio HTTP.
            throw new MlServiceException(
                    HttpStatus.CONFLICT,
                    "El entrenamiento se ejecuta por Render Cron Job; use Trigger Run en Render o espere la próxima ejecución");
        }
        return trainViaApi(request);
    }

    public MlPredictionsResponse score(MlScoreRequest request) {
        verificarHabilitado();
        if (esCron()) {
            // En modo CRON el scoring lo produce el Cron Job: devolvemos las últimas
            // predicciones desde Neon, lo más coherente para el frontend.
            log.info("score() en modo CRON: devolviendo últimas predicciones desde Neon");
            return leerUltimasPredicciones();
        }
        return scoreViaApi(request);
    }

    // ── Modo CRON: lecturas desde Neon ───────────────────────────────────────────

    private MlHealthResponse healthCron() {
        var ultima = mlMetricaRepository.findFirstByOrderByTsDesc();
        long totalPredicciones = mlPrediccionRepository.count();
        boolean disponibles = ultima.isPresent();
        String status = disponibles ? "ok" : "sin_datos";
        return new MlHealthResponse(
                status,
                "nexora-ml-cron",
                null,
                null,
                MlMode.CRON.name(),
                disponibles,
                ultima.map(MlMetrica::getTs).orElse(null),
                totalPredicciones);
    }

    private MlMetricsResponse metricsCron() {
        MlMetrica ultima = mlMetricaRepository.findFirstByOrderByTsDesc()
                .orElseThrow(() -> new MlServiceException(
                        HttpStatus.NOT_FOUND,
                        "Aún no hay métricas disponibles. El Cron Job de entrenamiento no ha registrado resultados todavía."));
        return MlMapper.toMetricsResponse(ultima, parseMatriz(ultima.getMatrizConfusion()));
    }

    private MlPredictionsResponse leerUltimasPredicciones() {
        List<MlPrediccion> filas =
                mlPrediccionRepository.findByOrderByTsDesc(PageRequest.of(0, MAX_PREDICCIONES));
        List<MlPrediction> predicciones = filas.stream().map(MlMapper::toPrediction).toList();
        String status = predicciones.isEmpty() ? "sin_datos" : "ok";
        return new MlPredictionsResponse(status, predicciones.size(), predicciones);
    }

    private List<List<Integer>> parseMatriz(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<List<Integer>>>() {});
        } catch (Exception e) {
            log.warn("No se pudo parsear matriz_confusion almacenada: {}", e.getMessage());
            return null;
        }
    }

    // ── Modo API: HTTP + traza en pipeline ───────────────────────────────────────

    private MlHealthResponse healthApi() {
        MlHealthResponse h = mlServiceClient.health();
        if (h == null) {
            return new MlHealthResponse("desconocido", "nexora-ml", null, null, MlMode.API.name(), null, null, null);
        }
        return new MlHealthResponse(
                h.status(), h.service(), h.version(), h.modeloCargado(),
                MlMode.API.name(), null, null, null);
    }

    private MlTrainResponse trainViaApi(MlTrainRequest request) {
        Pipeline pipeline = obtenerOCrearPipeline(
                PIPELINE_ENTRENAMIENTO, "Entrenamiento del modelo de IA (EntrenamientoAI)");
        PipelineEjecucion ejecucion = iniciarEjecucion(pipeline, "Entrenamiento ML en progreso");
        long inicioNanos = System.nanoTime();
        try {
            MlTrainResponse response = mlServiceClient.train(request);
            finalizarExitosa(ejecucion, inicioNanos, 0, "Entrenamiento ML completado");
            if (response != null && response.metricas() != null) {
                registrarKpis(ejecucion, response.metricas());
            }
            return response;
        } catch (MlServiceException e) {
            registrarFallo(ejecucion, inicioNanos, e);
            throw e;
        }
    }

    private MlPredictionsResponse scoreViaApi(MlScoreRequest request) {
        Pipeline pipeline = obtenerOCrearPipeline(
                PIPELINE_SCORING, "Scoring de registros con el modelo de IA (EntrenamientoAI)");
        PipelineEjecucion ejecucion = iniciarEjecucion(pipeline, "Scoring ML en progreso");
        long inicioNanos = System.nanoTime();
        try {
            MlPredictionsResponse response = mlServiceClient.score(request);
            finalizarExitosa(ejecucion, inicioNanos, contarProcesados(response), "Scoring ML completado");
            return response;
        } catch (MlServiceException e) {
            registrarFallo(ejecucion, inicioNanos, e);
            throw e;
        }
    }

    // ── Helpers de configuración ─────────────────────────────────────────────────

    private boolean esCron() {
        return mlProperties.getMode() == MlMode.CRON;
    }

    private void verificarHabilitado() {
        if (!mlProperties.isEnabled()) {
            throw new MlServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Integración ML desactivada. Configure NEXORA_ML_ENABLED=true para habilitarla.");
        }
    }

    // ── Helpers de pipeline ──────────────────────────────────────────────────────

    private Pipeline obtenerOCrearPipeline(String nombre, String descripcion) {
        return pipelineRepository.findFirstByNombre(nombre)
                .orElseGet(() -> pipelineRepository.save(Pipeline.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .tipo(TipoPipeline.ML)
                        .activo(Boolean.TRUE)
                        .build()));
    }

    private PipelineEjecucion iniciarEjecucion(Pipeline pipeline, String resumen) {
        return pipelineEjecucionRepository.save(PipelineEjecucion.builder()
                .pipeline(pipeline)
                .estado(EstadoPipelineEjecucion.EN_EJECUCION)
                .registrosProcesados(0)
                .erroresEncontrados(0)
                .iniciadoEn(LocalDateTime.now())
                .resumen(resumen)
                .build());
    }

    private void finalizarExitosa(PipelineEjecucion ejecucion, long inicioNanos, int procesados, String resumen) {
        ejecucion.setEstado(EstadoPipelineEjecucion.EXITOSA);
        ejecucion.setRegistrosProcesados(procesados);
        ejecucion.setErroresEncontrados(0);
        ejecucion.setDuracionMs(duracionMs(inicioNanos));
        ejecucion.setFinalizadoEn(LocalDateTime.now());
        ejecucion.setResumen(resumen);
        pipelineEjecucionRepository.save(ejecucion);
    }

    private void registrarFallo(PipelineEjecucion ejecucion, long inicioNanos, MlServiceException e) {
        ejecucion.setEstado(EstadoPipelineEjecucion.FALLIDA);
        ejecucion.setErroresEncontrados(1);
        ejecucion.setDuracionMs(duracionMs(inicioNanos));
        ejecucion.setFinalizadoEn(LocalDateTime.now());
        ejecucion.setResumen(truncar("Fallo en servicio ML: " + safe(e.getMessage(), ""), 1000));
        pipelineEjecucionRepository.save(ejecucion);

        pipelineErrorRepository.save(PipelineError.builder()
                .pipelineEjecucion(ejecucion)
                .mensaje(truncar(safe(e.getMessage(), "Error desconocido en servicio ML"), 300))
                .detalle(truncar("HTTP " + e.getStatus().value() + " — " + safe(e.getMessage(), ""), 2000))
                .build());

        log.warn("Ejecución ML id={} marcada como FALLIDA: {}", ejecucion.getId(), e.getMessage());
    }

    // ── Helpers de KPI ───────────────────────────────────────────────────────────

    private void registrarKpis(PipelineEjecucion ejecucion, MlMetricsResponse m) {
        LocalDate periodo = LocalDate.now();
        guardarKpi(ejecucion, TipoKpi.ML_ACCURACY, m.accuracy(), periodo);
        guardarKpi(ejecucion, TipoKpi.ML_PRECISION, m.precision(), periodo);
        guardarKpi(ejecucion, TipoKpi.ML_RECALL, m.recall(), periodo);
        guardarKpi(ejecucion, TipoKpi.ML_F1, m.f1(), periodo);
        guardarKpi(ejecucion, TipoKpi.ML_ROC_AUC, m.rocAuc(), periodo);
        guardarKpi(ejecucion, TipoKpi.ML_GINI, m.gini(), periodo);
    }

    private void guardarKpi(PipelineEjecucion ejecucion, TipoKpi tipo, Double valor, LocalDate periodo) {
        if (valor == null) {
            return;
        }
        kpiResultadoRepository.save(KpiResultado.builder()
                .pipelineEjecucion(ejecucion)
                .tipo(tipo)
                // KpiResultado.valor es NUMERIC(15,2): se redondea a 2 decimales.
                // El valor completo siempre está disponible vía GET /api/ml/metrics.
                .valor(BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP))
                .periodo(periodo)
                .build());
    }

    // ── Utilidades ───────────────────────────────────────────────────────────────

    private static int contarProcesados(MlPredictionsResponse response) {
        if (response == null) {
            return 0;
        }
        if (response.total() != null) {
            return response.total();
        }
        return response.predicciones() != null ? response.predicciones().size() : 0;
    }

    private static long duracionMs(long inicioNanos) {
        return Duration.ofNanos(System.nanoTime() - inicioNanos).toMillis();
    }

    private static String safe(String valor, String fallback) {
        return (valor == null || valor.isBlank()) ? fallback : valor;
    }

    private static String truncar(String valor, int max) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= max ? valor : valor.substring(0, max);
    }
}
