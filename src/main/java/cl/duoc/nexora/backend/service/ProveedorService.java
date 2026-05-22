package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.ProveedorRequest;
import cl.duoc.nexora.backend.dto.response.ProveedorResponse;
import cl.duoc.nexora.backend.enums.EstadoProveedor;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.ProveedorMapper;
import cl.duoc.nexora.backend.model.Proveedor;
import cl.duoc.nexora.backend.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public Page<ProveedorResponse> listar(EstadoProveedor estado, Pageable pageable) {
        if (estado != null) {
            return proveedorRepository.findByEstado(estado, pageable).map(ProveedorMapper::toResponse);
        }
        return proveedorRepository.findAll(pageable).map(ProveedorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id) {
        return ProveedorMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        Proveedor proveedor = ProveedorMapper.toEntity(request);
        log.info("Creando proveedor con rut {}", request.rut());
        return ProveedorMapper.toResponse(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = buscarPorId(id);
        ProveedorMapper.updateEntity(proveedor, request);
        log.info("Actualizando proveedor {}", id);
        return ProveedorMapper.toResponse(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor no encontrado: " + id);
        }
        log.info("Eliminando proveedor {}", id);
        proveedorRepository.deleteById(id);
    }

    private Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + id));
    }
}
