package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.SolicitudCompraRequest;
import cl.duoc.nexora.backend.dto.response.SolicitudCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.model.Usuario;

public final class SolicitudCompraMapper {

    private SolicitudCompraMapper() {
    }

    public static SolicitudCompra toEntity(SolicitudCompraRequest request, Usuario usuarioSolicitante) {
        SolicitudCompra solicitud = new SolicitudCompra();
        solicitud.setTitulo(request.titulo());
        solicitud.setDescripcion(request.descripcion());
        solicitud.setCategoria(request.categoria());
        solicitud.setMontoEstimado(request.montoEstimado());
        solicitud.setFechaRequerida(request.fechaRequerida());
        solicitud.setEstado(request.estado() != null ? request.estado() : EstadoSolicitudCompra.BORRADOR);
        solicitud.setUsuarioSolicitante(usuarioSolicitante);
        return solicitud;
    }

    public static void updateEntity(SolicitudCompra solicitud, SolicitudCompraRequest request) {
        solicitud.setTitulo(request.titulo());
        solicitud.setDescripcion(request.descripcion());
        solicitud.setCategoria(request.categoria());
        solicitud.setMontoEstimado(request.montoEstimado());
        solicitud.setFechaRequerida(request.fechaRequerida());
        if (request.estado() != null) {
            solicitud.setEstado(request.estado());
        }
    }

    public static SolicitudCompraResponse toResponse(SolicitudCompra solicitud) {
        Usuario usuario = solicitud.getUsuarioSolicitante();
        return new SolicitudCompraResponse(
                solicitud.getId(),
                solicitud.getTitulo(),
                solicitud.getDescripcion(),
                solicitud.getCategoria(),
                solicitud.getMontoEstimado(),
                solicitud.getFechaRequerida(),
                solicitud.getEstado(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNombre() : null,
                solicitud.getCreadoEn()
        );
    }
}
