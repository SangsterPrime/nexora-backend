package cl.duoc.nexora.backend.dto.error;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String mensaje,
        String path,
        LocalDateTime timestamp,
        Map<String, String> errores
) {
}
