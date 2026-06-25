package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.config.MlProperties;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlTrainRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainResponse;
import cl.duoc.nexora.backend.exception.MlServiceException;
import cl.duoc.nexora.backend.model.PipelineEjecucion;
import cl.duoc.nexora.backend.repository.KpiResultadoRepository;
import cl.duoc.nexora.backend.repository.PipelineEjecucionRepository;
import cl.duoc.nexora.backend.repository.PipelineErrorRepository;
import cl.duoc.nexora.backend.repository.PipelineRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class MlServiceTest {

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

    @InjectMocks
    private MlService mlService;

    // ── Caso: integración desactivada ──────────────────────────────────────────

    @Test
    void health_cuandoDeshabilitado_lanzaServiceUnavailableSinLlamarCliente() {
        when(mlProperties.isEnabled()).thenReturn(false);

        MlServiceException ex = assertThrows(MlServiceException.class, () -> mlService.health());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
        verifyNoInteractions(mlServiceClient);
    }

    // ── Caso: entrenamiento exitoso registra ejecución y KPIs ──────────────────

    @Test
    void train_exitoso_registraEjecucionExitosaYKpis() {
        when(mlProperties.isEnabled()).thenReturn(true);
        when(pipelineRepository.findFirstByNombre(anyString())).thenReturn(Optional.empty());
        when(pipelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pipelineEjecucionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MlMetricsResponse metricas = new MlMetricsResponse(
                0.95, 0.90, 0.92, 0.91, 0.97, 0.94, List.of(List.of(10, 2), List.of(1, 20)));
        when(mlServiceClient.train(any()))
                .thenReturn(new MlTrainResponse("ok", "entrenado", 100, 123L, metricas));

        MlTrainResponse response = mlService.train(new MlTrainRequest(null, null));

        assertNotNull(response);
        // init EN_EJECUCION + finalizar EXITOSA
        verify(pipelineEjecucionRepository, atLeast(2)).save(any(PipelineEjecucion.class));
        // 6 métricas escalares persistidas como KPI
        verify(kpiResultadoRepository, times(6)).save(any());
        verify(pipelineErrorRepository, never()).save(any());
    }

    // ── Caso: fallo del cliente registra PipelineError y re-lanza ──────────────

    @Test
    void train_cuandoClienteFalla_registraErrorYRelanza() {
        when(mlProperties.isEnabled()).thenReturn(true);
        when(pipelineRepository.findFirstByNombre(anyString())).thenReturn(Optional.empty());
        when(pipelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pipelineEjecucionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mlServiceClient.train(any()))
                .thenThrow(new MlServiceException(HttpStatus.BAD_GATEWAY, "servicio caído"));

        assertThrows(MlServiceException.class, () -> mlService.train(new MlTrainRequest(null, null)));

        verify(pipelineErrorRepository, times(1)).save(any());
        verify(kpiResultadoRepository, never()).save(any());
    }
}
