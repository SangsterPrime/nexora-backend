package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.TipoPipeline;
import java.time.LocalDateTime;

public record PipelineResponse(
        Long id,
        String nombre,
        String descripcion,
        TipoPipeline tipo,
        Boolean activo,
        LocalDateTime creadoEn
) {
}
