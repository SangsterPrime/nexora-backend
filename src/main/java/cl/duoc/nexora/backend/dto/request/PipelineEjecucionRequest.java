package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.EstadoPipelineEjecucion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record PipelineEjecucionRequest(
        @NotNull @Positive Long pipelineId,
        @Positive Long solicitudCompraId,
        EstadoPipelineEjecucion estado,
        @PositiveOrZero Integer registrosProcesados,
        @PositiveOrZero Integer erroresEncontrados,
        @PositiveOrZero Long duracionMs,
        LocalDateTime finalizadoEn,
        @Size(max = 1000) String resumen
) {
}
