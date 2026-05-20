package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.NegociacionRequest;
import cl.duoc.nexora.backend.dto.response.NegociacionResponse;
import cl.duoc.nexora.backend.enums.EstadoNegociacion;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.Negociacion;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;

public final class NegociacionMapper {

    private NegociacionMapper() {
    }

    public static Negociacion toEntity(NegociacionRequest request, Cotizacion cotizacion) {
        Negociacion negociacion = new Negociacion();
        negociacion.setCotizacion(cotizacion);
        negociacion.setMensaje(request.mensaje());
        negociacion.setMontoOfertado(request.montoOfertado());
        negociacion.setEstado(request.estado() != null ? request.estado() : EstadoNegociacion.ABIERTA);
        return negociacion;
    }

    public static void updateEntity(Negociacion negociacion, NegociacionRequest request, Cotizacion cotizacion) {
        negociacion.setCotizacion(cotizacion);
        negociacion.setMensaje(request.mensaje());
        negociacion.setMontoOfertado(request.montoOfertado());
        if (request.estado() != null) {
            negociacion.setEstado(request.estado());
        }
    }

    public static NegociacionResponse toResponse(Negociacion negociacion) {
        Cotizacion cotizacion = negociacion.getCotizacion();
        SolicitudCompra solicitud = cotizacion.getSolicitudCompra();
        Proveedor proveedor = cotizacion.getProveedor();
        return new NegociacionResponse(
                negociacion.getId(),
                cotizacion.getId(),
                solicitud.getId(),
                proveedor.getId(),
                proveedor.getRazonSocial(),
                negociacion.getMensaje(),
                negociacion.getMontoOfertado(),
                negociacion.getEstado(),
                negociacion.getCreadoEn()
        );
    }
}
