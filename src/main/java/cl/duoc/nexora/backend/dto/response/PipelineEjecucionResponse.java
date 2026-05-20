package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.EstadoPipelineEjecucion;
import java.time.LocalDateTime;

public record PipelineEjecucionResponse(
        Long id,
        Long pipelineId,
        String pipelineNombre,
        Long solicitudCompraId,
        String solicitudCompraTitulo,
        EstadoPipelineEjecucion estado,
        Integer registrosProcesados,
        Integer erroresEncontrados,
        Long duracionMs,
        LocalDateTime iniciadoEn,
        LocalDateTime finalizadoEn,
        String resumen
) {
}
