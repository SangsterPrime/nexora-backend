package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CotizacionResponse(
        Long id,
        Long solicitudCompraId,
        String solicitudCompraTitulo,
        Long proveedorId,
        String proveedorRazonSocial,
        BigDecimal monto,
        Integer plazoEntregaDias,
        String condiciones,
        BigDecimal riskScore,
        EstadoCotizacion estado,
        LocalDateTime creadoEn
) {
}
