package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudCompraResponse(
        Long id,
        String titulo,
        String descripcion,
        String categoria,
        BigDecimal montoEstimado,
        LocalDate fechaRequerida,
        EstadoSolicitudCompra estado,
        Long usuarioSolicitanteId,
        String usuarioSolicitanteNombre,
        LocalDateTime creadoEn
) {
}
