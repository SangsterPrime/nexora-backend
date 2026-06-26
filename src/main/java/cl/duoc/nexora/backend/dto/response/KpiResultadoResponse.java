package cl.duoc.nexora.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record KpiResultadoResponse(
        Long id,
        Long pipelineEjecucionId,
        String tipo,
        BigDecimal valor,
        LocalDate periodo,
        LocalDateTime calculadoEn
) {
}
