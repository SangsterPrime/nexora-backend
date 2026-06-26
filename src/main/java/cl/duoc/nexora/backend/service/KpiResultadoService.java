package cl.duoc.nexora.backend.service;

import cl.duoc.nexora.backend.dto.response.KpiResultadoResponse;
import cl.duoc.nexora.backend.repository.KpiResultadoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KpiResultadoService {

    private final KpiResultadoRepository kpiResultadoRepository;

    @Transactional(readOnly = true)
    public List<KpiResultadoResponse> listar() {
        return kpiResultadoRepository.findAll().stream()
                .map(k -> new KpiResultadoResponse(
                        k.getId(),
                        k.getPipelineEjecucion().getId(),
                        k.getTipo().name(),
                        k.getValor(),
                        k.getPeriodo(),
                        k.getCalculadoEn()
                ))
                .toList();
    }
}
