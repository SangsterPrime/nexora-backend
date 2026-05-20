package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CotizacionRequest(
        @NotNull @Positive Long solicitudCompraId,
        @NotNull @Positive Long proveedorId,
        @NotNull @PositiveOrZero BigDecimal monto,
        @Positive Integer plazoEntregaDias,
        @Size(max = 1000) String condiciones,
        @PositiveOrZero BigDecimal riskScore,
        EstadoCotizacion estado
) {
}
