package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.config.MlProperties;
import cl.duoc.nexora.backend.dto.ml.MlHealthResponse;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlPredictionsResponse;
import cl.duoc.nexora.backend.dto.ml.MlTrainRequest;
import cl.duoc.nexora.backend.enums.MlMode;
import cl.duoc.nexora.backend.exception.MlServiceException;
import cl.duoc.nexora.backend.model.MlMetrica;
import cl.duoc.nexora.backend.model.MlPrediccion;
import cl.duoc.nexora.backend.repository.KpiResultadoRepository;
import cl.duoc.nexora.backend.repository.MlMetricaRepository;
import cl.duoc.nexora.backend.repository.MlPrediccionRepository;
import cl.duoc.nexora.backend.repository.PipelineEjecucionRepository;
import cl.duoc.nexora.backend.repository.PipelineErrorRepository;
import cl.duoc.nexora.backend.repository.PipelineRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

/**
 * Tests del modo CRON de {@link MlService}: el backend lee desde Neon y nunca llama
 * al servicio HTTP. Se construye el service manualmente para inyectar un
 * {@link ObjectMapper} real (necesario para parsear {@code matriz_confusion}).
 */
@ExtendWith(MockitoExtension.class)
class MlServiceCronTest {

    @Mock
    private MlProperties mlProperties;
    @Mock
    private MlServiceClient mlServiceClient;
    @Mock
    private PipelineRepository pipelineRepository;
    @Mock
    private PipelineEjecucionRepository pipelineEjecucionRepository;
    @Mock
    private PipelineErrorRepository pipelineErrorRepository;
    @Mock
    private KpiResultadoRepository kpiResultadoRepository;
    @Mock
    private MlMetricaRepository mlMetricaRepository;
    @Mock
    private MlPrediccionRepository mlPrediccionRepository;

    private MlService mlService;

    @BeforeEach
    void setUp() {
        mlService = new MlService(
                mlProperties,
                mlServiceClient,
                pipelineRepository,
                pipelineEjecucionRepository,
                pipelineErrorRepository,
                kpiResultadoRepository,
                mlMetricaRepository,
                mlPrediccionRepository);
        when(mlProperties.isEnabled()).thenReturn(true);
        when(mlProperties.getMode()).thenReturn(MlMode.CRON);
    }

    // ── health con datos existentes ─────────────────────────────────────────────

    @Test
    void health_conDatos_reportaUltimaEjecucionYConteo() {
        LocalDateTime ts = LocalDateTime.of(2026, 6, 24, 10, 30);
        MlMetrica metrica = MlMetrica.builder().id(1L).ts(ts).accuracy(0.95).build();
        when(mlMetricaRepository.findFirstByOrderByTsDesc()).thenReturn(Optional.of(metrica));
        when(mlPrediccionRepository.count()).thenReturn(42L);

        MlHealthResponse response = mlService.health();

        assertEquals("ok", response.status());
        assertEquals("CRON", response.modo());
        assertEquals(Boolean.TRUE, response.metricasDisponibles());
        assertEquals(ts, response.ultimaEjecucion());
        assertEquals(42L, response.totalPredicciones());
        verifyNoInteractions(mlServiceClient);
    }

    // ── metrics sin datos devuelve error claro (404) ────────────────────────────

    @Test
    void metrics_sinDatos_lanzaNotFoundConMensajeClaro() {
        when(mlMetricaRepository.findFirstByOrderByTsDesc()).thenReturn(Optional.empty());

        MlServiceException ex = assertThrows(MlServiceException.class, () -> mlService.metrics());

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().toLowerCase().contains("métricas")
                || ex.getMessage().toLowerCase().contains("metricas"));
        verifyNoInteractions(mlServiceClient);
    }

    // ── metrics con datos mapea la última fila (incluida la matriz parseada) ─────

    @Test
    void metrics_conDatos_mapeaUltimaFilaYParseaMatriz() {
        MlMetrica metrica = MlMetrica.builder()
                .id(1L)
                .ts(LocalDateTime.now())
                .accuracy(0.95)
                .recall(0.90)
                .precision(0.92)
                .f1(0.91)
                .rocAuc(0.97)
                .gini(0.94)
                .matrizConfusion("[[10,2],[1,20]]")
                .build();
        when(mlMetricaRepository.findFirstByOrderByTsDesc()).thenReturn(Optional.of(metrica));

        MlMetricsResponse response = mlService.metrics();

        assertEquals(0.95, response.accuracy());
        assertEquals(0.97, response.rocAuc());
        assertEquals(List.of(List.of(10, 2), List.of(1, 20)), response.matrizConfusion());
    }

    // ── predictions con datos devuelve lista ────────────────────────────────────

    @Test
    void predictions_conDatos_devuelveLista() {
        List<MlPrediccion> filas = List.of(
                MlPrediccion.builder().id(1L).ts(LocalDateTime.now()).entidad("COTIZACION").score(0.8).build(),
                MlPrediccion.builder().id(2L).ts(LocalDateTime.now()).entidad("COTIZACION").score(0.3).build());
        when(mlPrediccionRepository.findByOrderByTsDesc(any(Pageable.class))).thenReturn(filas);

        MlPredictionsResponse response = mlService.predictions();

        assertEquals("ok", response.status());
        assertEquals(2, response.total());
        assertEquals(2, response.predicciones().size());
        verifyNoInteractions(mlServiceClient);
    }

    // ── train en modo CRON no llama al servicio HTTP y responde 409 ──────────────

    @Test
    void train_enModoCron_responde409SinLlamarServicioHttp() {
        MlServiceException ex =
                assertThrows(MlServiceException.class, () -> mlService.train(new MlTrainRequest(null, null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verifyNoInteractions(mlServiceClient);
    }
}
