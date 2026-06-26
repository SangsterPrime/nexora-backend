package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.ProveedorRequest;
import cl.duoc.nexora.backend.dto.response.ProveedorResponse;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.model.Proveedor;

public final class ProveedorMapper {

    private ProveedorMapper() {
    }

    public static Proveedor toEntity(ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRut(request.rut());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setNombreContacto(request.nombreContacto());
        proveedor.setEmail(request.email());
        proveedor.setTelefono(request.telefono());
        proveedor.setDireccion(request.direccion());
        proveedor.setReputacionScore(request.reputacionScore());
        proveedor.setCumplimientoScore(request.cumplimientoScore());
        proveedor.setEstado(request.estado() != null ? request.estado() : EstadoProveedor.ACTIVO);
        return proveedor;
    }

    public static void updateEntity(Proveedor proveedor, ProveedorRequest request) {
        proveedor.setRut(request.rut());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setNombreContacto(request.nombreContacto());
        proveedor.setEmail(request.email());
        proveedor.setTelefono(request.telefono());
        proveedor.setDireccion(request.direccion());
        proveedor.setReputacionScore(request.reputacionScore());
        proveedor.setCumplimientoScore(request.cumplimientoScore());
        if (request.estado() != null) {
            proveedor.setEstado(request.estado());
        }
    }

    public static ProveedorResponse toResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getRut(),
                proveedor.getRazonSocial(),
                proveedor.getNombreContacto(),
                proveedor.getEmail(),
                proveedor.getTelefono(),
                proveedor.getDireccion(),
                proveedor.getReputacionScore(),
                proveedor.getCumplimientoScore(),
                proveedor.getEstado(),
                proveedor.getCreadoEn()
        );
    }
}
