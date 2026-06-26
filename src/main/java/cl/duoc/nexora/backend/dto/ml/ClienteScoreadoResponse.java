package cl.duoc.nexora.backend.dto.ml;

import java.time.LocalDateTime;

public record ClienteScoreadoResponse(
        Long id,
        Integer edad,
        Integer anosCliente,
        Double usoDatosGb,
        Integer llamadasMes,
        Integer reclamos,
        Integer planPremium,
        Integer abandona,
        Double probAbandono,
        String segmentoRiesgo,
        String accionRetencion,
        LocalDateTime fechaCarga
) {
}
