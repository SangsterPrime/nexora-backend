package cl.duoc.nexora.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción de dominio para fallos al comunicarse con el servicio Python de IA
 * ({@code EntrenamientoAI}).
 *
 * <p>Lleva el {@link HttpStatus} que el backend debe devolver al frontend, con un
 * mensaje claro y <strong>sin filtrar secretos</strong> (la API key nunca se
 * incluye en el mensaje). Es manejada de forma centralizada por
 * {@code GlobalExceptionHandler}.</p>
 *
 * <ul>
 *   <li>{@link HttpStatus#SERVICE_UNAVAILABLE} (503) — integración desactivada o URL no configurada.</li>
 *   <li>{@link HttpStatus#BAD_GATEWAY} (502) — el servicio ML respondió con error o no fue alcanzable.</li>
 * </ul>
 */
public class MlServiceException extends RuntimeException {

    private final HttpStatus status;

    public MlServiceException(String message) {
        this(HttpStatus.BAD_GATEWAY, message);
    }

    public MlServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public MlServiceException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
