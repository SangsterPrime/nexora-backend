package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.OrdenCompraRequest;
import cl.duoc.nexora.backend.dto.response.OrdenCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoOrdenCompra;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.OrdenCompra;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;

public final class OrdenCompraMapper {

    private OrdenCompraMapper() {
    }

    public static OrdenCompra toEntity(
            OrdenCompraRequest request,
            SolicitudCompra solicitudCompra,
            Cotizacion cotizacionGanadora
    ) {
        OrdenCompra ordenCompra = new OrdenCompra();
        ordenCompra.setNumero(request.numero());
        ordenCompra.setSolicitudCompra(solicitudCompra);
        ordenCompra.setCotizacionGanadora(cotizacionGanadora);
        ordenCompra.setMontoTotal(request.montoTotal());
        ordenCompra.setEstado(request.estado() != null ? request.estado() : EstadoOrdenCompra.EMITIDA);
        return ordenCompra;
    }

    public static void updateEntity(
            OrdenCompra ordenCompra,
            OrdenCompraRequest request,
            SolicitudCompra solicitudCompra,
            Cotizacion cotizacionGanadora
    ) {
        ordenCompra.setNumero(request.numero());
        ordenCompra.setSolicitudCompra(solicitudCompra);
        ordenCompra.setCotizacionGanadora(cotizacionGanadora);
        ordenCompra.setMontoTotal(request.montoTotal());
        if (request.estado() != null) {
            ordenCompra.setEstado(request.estado());
        }
    }

    public static OrdenCompraResponse toResponse(OrdenCompra ordenCompra) {
        SolicitudCompra solicitud = ordenCompra.getSolicitudCompra();
        Cotizacion cotizacion = ordenCompra.getCotizacionGanadora();
        Proveedor proveedor = cotizacion.getProveedor();
        return new OrdenCompraResponse(
                ordenCompra.getId(),
                ordenCompra.getNumero(),
                solicitud.getId(),
                solicitud.getTitulo(),
                cotizacion.getId(),
                proveedor.getId(),
                proveedor.getRazonSocial(),
                ordenCompra.getMontoTotal(),
                ordenCompra.getEstado(),
                ordenCompra.getFechaEmision()
        );
    }
}
