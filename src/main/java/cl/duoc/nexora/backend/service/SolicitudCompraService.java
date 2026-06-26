package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.integration.N8nEventRequest;
import cl.duoc.nexora.backend.dto.request.SolicitudCompraRequest;
import cl.duoc.nexora.backend.dto.response.SolicitudCompraResponse;
import cl.duoc.nexora.backend.enums.EstadoSolicitudCompra;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.SolicitudCompraMapper;
import cl.duoc.nexora.backend.model.SolicitudCompra;
import cl.duoc.nexora.backend.model.Usuario;
import cl.duoc.nexora.backend.repository.SolicitudCompraRepository;
import cl.duoc.nexora.backend.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudCompraService {

    private final SolicitudCompraRepository solicitudCompraRepository;
    private final UsuarioRepository usuarioRepository;
    private final N8nIntegrationService n8nIntegrationService;

    @Transactional(readOnly = true)
    public Page<SolicitudCompraResponse> listar(
            EstadoSolicitudCompra estado,
            Long usuarioSolicitanteId,
            Pageable pageable
    ) {
        if (estado != null && usuarioSolicitanteId != null) {
            return solicitudCompraRepository.findByEstadoAndUsuarioSolicitanteId(estado, usuarioSolicitanteId, pageable)
                    .map(SolicitudCompraMapper::toResponse);
        }
        if (estado != null) {
            return solicitudCompraRepository.findByEstado(estado, pageable).map(SolicitudCompraMapper::toResponse);
        }
        if (usuarioSolicitanteId != null) {
            return solicitudCompraRepository.findByUsuarioSolicitanteId(usuarioSolicitanteId, pageable)
                    .map(SolicitudCompraMapper::toResponse);
        }
        return solicitudCompraRepository.findAll(pageable).map(SolicitudCompraMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SolicitudCompraResponse obtenerPorId(Long id) {
        return SolicitudCompraMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public SolicitudCompraResponse crear(SolicitudCompraRequest request) {
        Usuario usuarioSolicitante = buscarUsuarioOpcional(request.usuarioSolicitanteId());
        SolicitudCompra solicitud = SolicitudCompraMapper.toEntity(request, usuarioSolicitante);
        log.info("Creando solicitud de compra para usuario {}", request.usuarioSolicitanteId());
        SolicitudCompra guardada = solicitudCompraRepository.save(solicitud);

        // Notificar a n8n de forma no bloqueante.
        // Si n8n está caído o desactivado, la solicitud igual se crea correctamente.
        try {
            n8nIntegrationService.enviarEvento(buildEventoCreacion(guardada, usuarioSolicitante));
        } catch (Exception e) {
            log.warn("No se pudo notificar a n8n sobre la solicitud {}: {}", guardada.getId(), e.getMessage());
        }

        return SolicitudCompraMapper.toResponse(guardada);
    }

    @Transactional
    public SolicitudCompraResponse actualizar(Long id, SolicitudCompraRequest request) {
        SolicitudCompra solicitud = buscarPorId(id);
        validarUsuarioSolicitanteInmutable(solicitud, request.usuarioSolicitanteId());
        SolicitudCompraMapper.updateEntity(solicitud, request);
        log.info("Actualizando solicitud de compra {}", id);
        return SolicitudCompraMapper.toResponse(solicitudCompraRepository.save(solicitud));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!solicitudCompraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solicitud de compra no encontrada: " + id);
        }
        log.info("Eliminando solicitud de compra {}", id);
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

    private void validarUsuarioSolicitanteInmutable(SolicitudCompra solicitud, Long usuarioSolicitanteId) {
        Long usuarioActualId = solicitud.getUsuarioSolicitante() != null
                ? solicitud.getUsuarioSolicitante().getId()
                : null;
        if (usuarioSolicitanteId != null && !usuarioSolicitanteId.equals(usuarioActualId)) {
            throw new IllegalArgumentException("No se puede cambiar el usuario solicitante de una solicitud existente");
        }
    }

    /**
     * Construye el evento n8n para una solicitud recién creada.
     * Incluye los campos más relevantes sin exponer datos sensibles.
     */
    private N8nEventRequest buildEventoCreacion(SolicitudCompra solicitud, Usuario usuario) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", solicitud.getId());
        payload.put("titulo", solicitud.getTitulo());
        payload.put("estado", solicitud.getEstado() != null ? solicitud.getEstado().name() : null);
        payload.put("categoria", solicitud.getCategoria());
        payload.put("montoEstimado", solicitud.getMontoEstimado());
        payload.put("fechaRequerida", solicitud.getFechaRequerida() != null
                ? solicitud.getFechaRequerida().toString() : null);
        payload.put("creadoEn", solicitud.getCreadoEn() != null
                ? solicitud.getCreadoEn().toString() : null);

        String emailUsuario = usuario != null ? usuario.getEmail() : null;

        return N8nEventRequest.builder()
                .evento("SOLICITUD_COMPRA_CREADA")
                .entidad("SOLICITUD_COMPRA")
                .entidadId(solicitud.getId())
                .accion("CREATE")
                .usuarioEmail(emailUsuario)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
