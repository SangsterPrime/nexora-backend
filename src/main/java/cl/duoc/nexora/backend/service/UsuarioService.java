package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.request.UsuarioRequest;
import cl.duoc.nexora.backend.dto.response.UsuarioResponse;
import cl.duoc.nexora.backend.exception.ResourceNotFoundException;
import cl.duoc.nexora.backend.mapper.UsuarioMapper;
import cl.duoc.nexora.backend.model.Usuario;
import cl.duoc.nexora.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Boolean activo, Pageable pageable) {
        if (activo != null) {
            return usuarioRepository.findByActivo(activo, pageable).map(UsuarioMapper::toResponse);
        }
        return usuarioRepository.findAll(pageable).map(UsuarioMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        Usuario usuario = UsuarioMapper.toEntity(request);
        log.info("Creando usuario con email {}", request.email());
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        UsuarioMapper.updateEntity(usuario, request);
        log.info("Actualizando usuario {}", id);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        log.info("Eliminando usuario {}", id);
        usuarioRepository.deleteById(id);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
