package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.EstadoOrdenCompra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OrdenCompraRequest(
        @NotBlank @Size(max = 40) String numero,
        @NotNull @Positive Long solicitudCompraId,
        @NotNull @Positive Long cotizacionGanadoraId,
        @NotNull @PositiveOrZero BigDecimal montoTotal,
        EstadoOrdenCompra estado
) {
}
