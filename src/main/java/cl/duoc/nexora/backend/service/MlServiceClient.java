package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.config.MlProperties;
import cl.duoc.nexora.backend.dto.ml.MlHealthResponse;
import cl.duoc.nexora.backend.dto.ml.MlMetricsResponse;
import cl.duoc.nexora.backend.dto.ml.MlPredictionsResponse;
import cl.duoc.nexora.backend.dto.ml.MlScoreRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainRequest;
import cl.duoc.nexora.backend.dto.ml.MlTrainResponse;
import cl.duoc.nexora.backend.exception.MlServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente HTTP de bajo nivel para el servicio Python de IA ({@code EntrenamientoAI}).
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Resolver la URL base desde {@link MlProperties} y adjuntar el header
 *       {@code X-API-Key} en cada llamada.</li>
 *   <li>Deserializar las respuestas en los DTOs de {@code dto.ml}.</li>
 *   <li>Traducir cualquier fallo (HTTP 4xx/5xx o de conexión) a una
 *       {@link MlServiceException} con un mensaje claro para el frontend.</li>
 * </ul>
 *
 * <p>La API key <strong>nunca</strong> se escribe en los logs ni se propaga en los
 * mensajes de error.</p>
 */
@Service
@Slf4j
public class MlServiceClient {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final MlProperties mlProperties;
    private final RestClient restClient;

    /** Constructor principal usado por Spring Boot. */
    @Autowired
    public MlServiceClient(MlProperties mlProperties) {
        this.mlProperties = mlProperties;
        this.restClient = RestClient.create();
    }

    /** Constructor de paquete reservado para tests unitarios (permite inyectar un {@link RestClient}). */
    MlServiceClient(MlProperties mlProperties, RestClient restClient) {
        this.mlProperties = mlProperties;
        this.restClient = restClient;
    }

    // ── API pública ────────────────────────────────────────────────────────────

    public MlHealthResponse health() {
        return get("/health", MlHealthResponse.class);
    }

    public MlMetricsResponse metrics() {
        return get("/metrics", MlMetricsResponse.class);
    }

    public MlPredictionsResponse predictions() {
        return get("/predictions", MlPredictionsResponse.class);
    }

    public MlTrainResponse train(MlTrainRequest request) {
        return post("/train", request, MlTrainResponse.class);
    }

    public MlPredictionsResponse score(MlScoreRequest request) {
        return post("/score", request, MlPredictionsResponse.class);
    }

    // ── Helpers HTTP ─────────────────────────────────────────────────────────────

    private <T> T get(String path, Class<T> type) {
        String base = baseUrl();
        try {
            return restClient.get()
                    .uri(base + path)
                    .header(API_KEY_HEADER, mlProperties.getApiKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw downstreamError(path, e);
        } catch (RestClientException e) {
            throw connectionError(path, e);
        }
    }

    private <T> T post(String path, Object body, Class<T> type) {
        String base = baseUrl();
        try {
            return restClient.post()
                    .uri(base + path)
                    .header(API_KEY_HEADER, mlProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw downstreamError(path, e);
        } catch (RestClientException e) {
            throw connectionError(path, e);
        }
    }

    private String baseUrl() {
        String url = mlProperties.getUrl();
        if (url == null || url.isBlank()) {
            log.warn("NEXORA_ML_URL no configurada — no se puede llamar al servicio ML");
            throw new MlServiceException(HttpStatus.SERVICE_UNAVAILABLE, "NEXORA_ML_URL no configurada");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private MlServiceException downstreamError(String path, RestClientResponseException e) {
        // Logueamos el status pero nunca el header con la API key.
        log.warn("Servicio ML respondió {} en {}", e.getStatusCode().value(), path);
        return new MlServiceException(
                HttpStatus.BAD_GATEWAY,
                "El servicio de IA respondió con error " + e.getStatusCode().value(),
                e
        );
    }

    private MlServiceException connectionError(String path, RestClientException e) {
        log.warn("No se pudo conectar con el servicio ML en {}: {}", path, e.getMessage());
        return new MlServiceException(
                HttpStatus.BAD_GATEWAY,
                "No se pudo conectar con el servicio de IA",
                e
        );
    }
}
