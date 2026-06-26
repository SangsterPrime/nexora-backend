package cl.duoc.nexora.backend.controller;

import cl.duoc.nexora.backend.dto.response.KpiResultadoResponse;
import cl.duoc.nexora.backend.service.KpiResultadoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kpi-resultados")
@RequiredArgsConstructor
public class KpiResultadoController {

    private final KpiResultadoService kpiResultadoService;

    @GetMapping
    public List<KpiResultadoResponse> listar() {
        return kpiResultadoService.listar();
    }
}
