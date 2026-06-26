package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.EstadoNegociacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NegociacionResponse(
        Long id,
        Long cotizacionId,
        Long solicitudCompraId,
        Long proveedorId,
        String proveedorRazonSocial,
        String mensaje,
        BigDecimal montoOfertado,
        EstadoNegociacion estado,
        LocalDateTime creadoEn
) {
}
