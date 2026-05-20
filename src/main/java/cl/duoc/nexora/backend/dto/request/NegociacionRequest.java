package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.EstadoNegociacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record NegociacionRequest(
        @NotNull @Positive Long cotizacionId,
        @NotBlank @Size(max = 1000) String mensaje,
        @PositiveOrZero BigDecimal montoOfertado,
        EstadoNegociacion estado
) {
}
