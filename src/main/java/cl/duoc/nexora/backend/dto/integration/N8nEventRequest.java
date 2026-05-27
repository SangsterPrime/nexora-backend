package cl.duoc.nexora.backend.dto.integration;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Payload enviado al webhook de n8n cuando ocurre un evento en Nexora.
 *
 * <p>Ejemplo de serialización:</p>
 * <pre>
 * {
 *   "evento": "SOLICITUD_COMPRA_CREADA",
 *   "entidad": "SOLICITUD_COMPRA",
 *   "entidadId": 42,
 *   "accion": "CREATE",
 *   "usuarioEmail": "user@nexora.cl",
 *   "payload": { "titulo": "Compra de notebooks", "estado": "ABIERTA" },
 *   "timestamp": "2026-05-27T10:30:00"
 * }
 * </pre>
 */
@Getter
@Builder
public class N8nEventRequest {

    /** Nombre del evento negocio, p. ej. {@code SOLICITUD_COMPRA_CREADA}. */
    private final String evento;

    /** Entidad afectada, p. ej. {@code SOLICITUD_COMPRA}. */
    private final String entidad;

    /** ID de la entidad afectada. */
    private final Long entidadId;

    /** Tipo de acción: {@code CREATE}, {@code UPDATE}, {@code DELETE}, {@code TEST}. */
    private final String accion;

    /** Email del usuario que desencadenó el evento (puede ser null). */
    private final String usuarioEmail;

    /** Datos adicionales relevantes para el workflow de n8n. */
    private final Map<String, Object> payload;

    /** Marca temporal del evento en el backend. */
    private final LocalDateTime timestamp;
}
