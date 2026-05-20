package cl.duoc.nexora.backend.dto.response;

import java.util.Map;

public record ErrorResponse(String mensaje, Map<String, String> errores) {
}
