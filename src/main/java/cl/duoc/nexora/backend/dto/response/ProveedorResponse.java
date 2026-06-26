package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.EstadoProveedor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProveedorResponse(
        Long id,
        String rut,
        String razonSocial,
        String nombreContacto,
        String email,
        String telefono,
        String direccion,
        BigDecimal reputacionScore,
        BigDecimal cumplimientoScore,
        EstadoProveedor estado,
        LocalDateTime creadoEn
) {
}
