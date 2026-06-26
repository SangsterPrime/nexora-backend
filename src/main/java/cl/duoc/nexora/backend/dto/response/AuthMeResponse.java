package cl.duoc.nexora.backend.dto.response;

import cl.duoc.nexora.backend.enums.RolUsuario;

public record AuthMeResponse(
        Long id,
        String nombre,
        String email,
        RolUsuario rol,
        Boolean activo,
        String fotoUrl,
        boolean autenticado
) {
}
