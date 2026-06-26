package cl.duoc.nexora.backend.dto.request;

import cl.duoc.nexora.backend.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 160) String email,
        RolUsuario rol,
        Boolean activo
) {
}
