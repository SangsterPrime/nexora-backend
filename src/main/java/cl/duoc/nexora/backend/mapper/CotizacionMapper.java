package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.CotizacionRequest;
import cl.duoc.nexora.backend.dto.response.CotizacionResponse;
import cl.duoc.nexora.backend.enums.EstadoCotizacion;
import cl.duoc.nexora.backend.model.Cotizacion;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.model.SolicitudCompra;

public final class CotizacionMapper {

    private CotizacionMapper() {
    }

    public static Cotizacion toEntity(CotizacionRequest request, SolicitudCompra solicitudCompra, Proveedor proveedor) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setSolicitudCompra(solicitudCompra);
        cotizacion.setProveedor(proveedor);
        cotizacion.setMonto(request.monto());
        cotizacion.setPlazoEntregaDias(request.plazoEntregaDias());
        cotizacion.setCondiciones(request.condiciones());
        cotizacion.setRiskScore(request.riskScore());
        cotizacion.setEstado(request.estado() != null ? request.estado() : EstadoCotizacion.RECIBIDA);
        return cotizacion;
    }

    public static void updateEntity(Cotizacion cotizacion, CotizacionRequest request) {
        cotizacion.setMonto(request.monto());
        cotizacion.setPlazoEntregaDias(request.plazoEntregaDias());
        cotizacion.setCondiciones(request.condiciones());
        cotizacion.setRiskScore(request.riskScore());
        if (request.estado() != null) {
            cotizacion.setEstado(request.estado());
        }
    }

    public static CotizacionResponse toResponse(Cotizacion cotizacion) {
        SolicitudCompra solicitud = cotizacion.getSolicitudCompra();
        Proveedor proveedor = cotizacion.getProveedor();
        return new CotizacionResponse(
                cotizacion.getId(),
                solicitud.getId(),
                solicitud.getTitulo(),
                proveedor.getId(),
                proveedor.getRazonSocial(),
                cotizacion.getMonto(),
                cotizacion.getPlazoEntregaDias(),
                cotizacion.getCondiciones(),
                cotizacion.getRiskScore(),
                cotizacion.getEstado(),
                cotizacion.getCreadoEn()
        );
    }
}
