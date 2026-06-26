package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.ml.ClienteScoreadoResponse;
import cl.duoc.nexora.backend.dto.ml.MlHealthResponse;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlPredictionsResponse;
import cl.duoc.nexora.backend.dto.ml.MlScoreRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainResponse;
import cl.duoc.nexora.backend.service.ClienteScoreadoService;
import cl.duoc.nexora.backend.service.MlService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fachada REST hacia el servicio Python de IA ({@code EntrenamientoAI}).
 *
 * <p>Ruta base: {@code /api/ml}. El frontend siempre habla con estos endpoints,
 * nunca directamente con el servicio Python; así la API key permanece en el
 * backend y nunca se expone al navegador.</p>
 *
 * <p>Requiere autenticación. Los fallos del servicio ML son traducidos a
 * respuestas claras por {@code GlobalExceptionHandler} ({@code 502} si el
 * servicio falla, {@code 503} si la integración está desactivada o sin
 * configurar).</p>
 */
@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MlController {

    private final MlService mlService;
    private final ClienteScoreadoService clienteScoreadoService;

    /** Verifica el estado del servicio Python. */
    @GetMapping("/health")
    public MlHealthResponse health() {
        return mlService.health();
    }

    /** Dispara un entrenamiento del modelo y registra la ejecución en pipeline. */
    @PostMapping("/train")
    public MlTrainResponse train(@RequestBody(required = false) MlTrainRequest request) {
        return mlService.train(request != null ? request : new MlTrainRequest(null, null));
    }

    /** Scorea registros con el modelo y registra la ejecución en pipeline. */
    @PostMapping("/score")
    public MlPredictionsResponse score(@RequestBody(required = false) MlScoreRequest request) {
        return mlService.score(request != null ? request : new MlScoreRequest(null, null));
    }

    /** Obtiene las métricas del último modelo entrenado. */
    @GetMapping("/metrics")
    public MlMetricsResponse metrics() {
        return mlService.metrics();
    }

    /** Obtiene las predicciones/resultados scoreados almacenados por el servicio. */
    @GetMapping("/predictions")
    public MlPredictionsResponse predictions() {
        return mlService.predictions();
    }

    /** Devuelve todos los clientes scoreados desde la tabla clientes_scoreados (datos ricos con accion_retencion). */
    @GetMapping("/clientes-scoreados")
    public List<ClienteScoreadoResponse> clientesScoreados() {
        return clienteScoreadoService.listar();
    }
}
