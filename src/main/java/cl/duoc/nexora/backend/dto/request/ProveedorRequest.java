package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.EstadoProveedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProveedorRequest(
        @NotBlank @Size(max = 20) String rut,
        @NotBlank @Size(max = 160) String razonSocial,
        @Size(max = 120) String nombreContacto,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(max = 30) String telefono,
        @Size(max = 250) String direccion,
        @PositiveOrZero BigDecimal reputacionScore,
        @PositiveOrZero BigDecimal cumplimientoScore,
        EstadoProveedor estado
) {
}
