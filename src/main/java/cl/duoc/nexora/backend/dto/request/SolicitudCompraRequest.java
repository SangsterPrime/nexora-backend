package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitudCompraRequest(
        @NotBlank @Size(max = 160) String titulo,
        @Size(max = 1000) String descripcion,
        @Size(max = 120) String categoria,
        @NotNull @PositiveOrZero BigDecimal montoEstimado,
        @FutureOrPresent LocalDate fechaRequerida,
        EstadoSolicitudCompra estado,
        @Positive Long usuarioSolicitanteId
) {
}
