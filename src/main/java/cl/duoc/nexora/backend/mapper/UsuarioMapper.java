package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.UsuarioRequest;
import cl.duoc.nexora.backend.dto.response.UsuarioResponse;
import cl.duoc.nexora.backend.enums.RolUsuario;
import cl.duoc.nexora.backend.model.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toEntity(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol() != null ? request.rol() : RolUsuario.COMPRADOR);
        usuario.setActivo(request.activo() != null ? request.activo() : Boolean.TRUE);
        return usuario;
    }

    public static void updateEntity(Usuario usuario, UsuarioRequest request) {
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }
    }

    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getCreadoEn()
        );
    }
}
