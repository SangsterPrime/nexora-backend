package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.ml.ClienteScoreadoResponse;
import cl.duoc.nexora.backend.repository.ClienteScoreadoRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteScoreadoService {

    private final ClienteScoreadoRepository repo;

    @Transactional(readOnly = true)
    public List<ClienteScoreadoResponse> listar() {
        try {
            return repo.findAllOrderByRiescoDesc().stream()
                    .map(c -> new ClienteScoreadoResponse(
                            c.getId(),
                            c.getEdad(),
                            c.getAnosCliente(),
                            c.getUsoDatosGb(),
                            c.getLlamadasMes(),
                            c.getReclamos(),
                            c.getPlanPremium(),
                            c.getAbandona(),
                            c.getProbAbandono(),
                            c.getSegmentoRiesgo(),
                            c.getAccionRetencion(),
                            c.getFechaCarga()
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("No se pudo leer clientes_scoreados (¿tabla no existe aún?): {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
