package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.EstadoOrdenCompra;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenCompraResponse(
        Long id,
        String numero,
        Long solicitudCompraId,
        String solicitudCompraTitulo,
        Long cotizacionGanadoraId,
        Long proveedorGanadorId,
        String proveedorGanadorRazonSocial,
        BigDecimal montoTotal,
        EstadoOrdenCompra estado,
        LocalDateTime fechaEmision
) {
}
