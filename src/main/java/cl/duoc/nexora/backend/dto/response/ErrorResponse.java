package cl.duoc.nexora.backend.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String path,
        String mensaje,
        Map<String, String> errores
) {
}
