package cl.duoc.nexora.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import cl.duoc.nexora.backend.config.MlProperties;
import cl.duoc.nexora.backend.exception.MlServiceException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MlServiceClientTest {

    @Mock
    private MlProperties mlProperties;

    // ── Caso 1: URL no configurada ─────────────────────────────────────────────

    @Test
    void health_cuandoUrlNoConfigurada_lanzaServiceUnavailable() {
        when(mlProperties.getUrl()).thenReturn("");
        MlServiceClient client = new MlServiceClient(mlProperties, RestClient.create());

        MlServiceException ex = assertThrows(MlServiceException.class, client::health);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    // ── Caso 2: servicio ML inalcanzable ───────────────────────────────────────

    @Test
    void health_cuandoConexionFalla_lanzaBadGateway() {
        when(mlProperties.getUrl()).thenReturn("http://ml-inexistente.local");
        when(mlProperties.getApiKey()).thenReturn("test-api-key");

        // RestClient con interceptor que simula un fallo de conexión.
        RestClient failingClient = RestClient.builder()
                .requestInterceptors(interceptors ->
                        interceptors.add((request, body, execution) -> {
                            throw new IOException("Conexión rechazada simulada");
                        }))
                .build();

        MlServiceClient client = new MlServiceClient(mlProperties, failingClient);

        MlServiceException ex = assertThrows(MlServiceException.class, client::health);

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
    }
}
