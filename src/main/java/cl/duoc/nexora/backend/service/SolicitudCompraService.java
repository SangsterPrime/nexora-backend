package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.SolicitudCompraRequest;
import cl.duoc.nexora.backend.dto.response.SolicitudCompraResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.SolicitudCompraMapper;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.model.Usuario;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import cl.duoc.nexora.backend.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitudCompraService {

    private final SolicitudCompraRepository solicitudCompraRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<SolicitudCompraResponse> listar() {
        return solicitudCompraRepository.findAll().stream()
                .map(SolicitudCompraMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudCompraResponse obtenerPorId(Long id) {
        return SolicitudCompraMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public SolicitudCompraResponse crear(SolicitudCompraRequest request) {
        Usuario usuarioSolicitante = buscarUsuarioOpcional(request.usuarioSolicitanteId());
        SolicitudCompra solicitud = SolicitudCompraMapper.toEntity(request, usuarioSolicitante);
        return SolicitudCompraMapper.toResponse(solicitudCompraRepository.save(solicitud));
    }

    @Transactional
    public SolicitudCompraResponse actualizar(Long id, SolicitudCompraRequest request) {
        SolicitudCompra solicitud = buscarPorId(id);
        Usuario usuarioSolicitante = buscarUsuarioOpcional(request.usuarioSolicitanteId());
        SolicitudCompraMapper.updateEntity(solicitud, request, usuarioSolicitante);
        return SolicitudCompraMapper.toResponse(solicitudCompraRepository.save(solicitud));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!solicitudCompraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solicitud de compra no encontrada: " + id);
        }
        solicitudCompraRepository.deleteById(id);
    }

    private SolicitudCompra buscarPorId(Long id) {
        return solicitudCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de compra no encontrada: " + id));
    }

    private Usuario buscarUsuarioOpcional(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
    }
}
