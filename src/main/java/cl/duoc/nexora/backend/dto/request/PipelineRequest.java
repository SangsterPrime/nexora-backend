package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.TipoPipeline;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PipelineRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull TipoPipeline tipo,
        Boolean activo
) {
}
