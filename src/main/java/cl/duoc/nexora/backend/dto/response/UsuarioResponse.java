package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.RolUsuario;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        RolUsuario rol,
        Boolean activo,
        LocalDateTime creadoEn
) {
}
