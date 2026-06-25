package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.config.MlProperties;
import cl.duoc.nexora.backend.dto.ml.MlHealthResponse;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlPredictionsResponse;
import cl.duoc.nexora.backend.dto.ml.MlScoreRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainResponse;
import cl.duoc.nexora.backend.enums.EstadoPipelineEjecucion;
import cl.duoc.nexora.backend.enums.TipoKpi;
import cl.duoc.nexora.backend.enums.TipoPipeline;
import cl.duoc.nexora.backend.exception.MlServiceException;
import cl.duoc.nexora.backend.model.KpiResultado;
import cl.duoc.nexora.backend.model.Pipeline;
import cl.duoc.nexora.backend.model.PipelineEjecucion;
import cl.duoc.nexora.backend.model.PipelineError;
import cl.duoc.nexora.backend.repository.KpiResultadoRepository;
import cl.duoc.nexora.backend.repository.PipelineEjecucionRepository;
import cl.duoc.nexora.backend.repository.PipelineErrorRepository;
import cl.duoc.nexora.backend.repository.PipelineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Orquesta la integración con el servicio Python de IA y deja traza de cada
 * ejecución en las entidades de pipeline existentes.
 *
 * <ul>
 *   <li>{@code train} y {@code score} se registran como {@link PipelineEjecucion}.</li>
 *   <li>Un fallo del servicio ML genera un {@link PipelineError} y marca la
 *       ejecución como {@link EstadoPipelineEjecucion#FALLIDA}, además de
 *       re-lanzar la {@link MlServiceException} para que el frontend reciba un
 *       error claro.</li>
 *   <li>Las métricas del modelo se persisten como {@link KpiResultado}.</li>
 *   <li>{@code health}, {@code metrics} y {@code predictions} son lecturas y no
 *       generan ejecuciones.</li>
 * </ul>
 *
 * <p>Las llamadas de escritura no son transaccionales a propósito: cada
 * {@code save} confirma de inmediato, de modo que el registro de error sobrevive
 * aunque se re-lance la excepción.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MlService {

    private static final String PIPELINE_ENTRENAMIENTO = "ML - Entrenamiento";
    private static final String PIPELINE_SCORING = "ML - Scoring";

    private final MlProperties mlProperties;
    private final MlServiceClient mlServiceClient;
    private final PipelineRepository pipelineRepository;
    private final PipelineEjecucionRepository pipelineEjecucionRepository;
    private final PipelineErrorRepository pipelineErrorRepository;
    private final KpiResultadoRepository kpiResultadoRepository;

    // ── Lecturas (sin registro de ejecución) ─────────────────────────────────────

    public MlHealthResponse health() {
        verificarHabilitado();
        return mlServiceClient.health();
    }

    public MlMetricsResponse metrics() {
        verificarHabilitado();
        return mlServiceClient.metrics();
    }

    public MlPredictionsResponse predictions() {
        verificarHabilitado();
        return mlServiceClient.predictions();
    }

    // ── Ejecuciones (con traza en pipeline) ──────────────────────────────────────

    public MlTrainResponse train(MlTrainRequest request) {
        verificarHabilitado();
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

    public MlPredictionsResponse score(MlScoreRequest request) {
        verificarHabilitado();
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
