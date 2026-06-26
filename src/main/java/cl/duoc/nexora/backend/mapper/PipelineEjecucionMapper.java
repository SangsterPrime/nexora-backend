package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.PipelineEjecucionRequest;
import cl.duoc.nexora.backend.dto.response.PipelineEjecucionResponse;
import cl.duoc.nexora.backend.enums.EstadoPipelineEjecucion;
import cl.duoc.nexora.backend.model.Pipeline;
import cl.duoc.nexora.backend.model.PipelineEjecucion;
import cl.duoc.nexora.backend.model.SolicitudCompra;

public final class PipelineEjecucionMapper {

    private PipelineEjecucionMapper() {
    }

    public static PipelineEjecucion toEntity(
            PipelineEjecucionRequest request,
            Pipeline pipeline,
            SolicitudCompra solicitudCompra
    ) {
        PipelineEjecucion ejecucion = new PipelineEjecucion();
        ejecucion.setPipeline(pipeline);
        ejecucion.setSolicitudCompra(solicitudCompra);
        ejecucion.setEstado(request.estado() != null ? request.estado() : EstadoPipelineEjecucion.PENDIENTE);
        ejecucion.setRegistrosProcesados(request.registrosProcesados() != null ? request.registrosProcesados() : 0);
        ejecucion.setErroresEncontrados(request.erroresEncontrados() != null ? request.erroresEncontrados() : 0);
        ejecucion.setDuracionMs(request.duracionMs());
        ejecucion.setFinalizadoEn(request.finalizadoEn());
        ejecucion.setResumen(request.resumen());
        return ejecucion;
    }

    public static void updateEntity(
            PipelineEjecucion ejecucion,
            PipelineEjecucionRequest request,
            Pipeline pipeline,
            SolicitudCompra solicitudCompra
    ) {
        ejecucion.setPipeline(pipeline);
        ejecucion.setSolicitudCompra(solicitudCompra);
        if (request.estado() != null) {
            ejecucion.setEstado(request.estado());
        }
        ejecucion.setRegistrosProcesados(request.registrosProcesados() != null ? request.registrosProcesados() : 0);
        ejecucion.setErroresEncontrados(request.erroresEncontrados() != null ? request.erroresEncontrados() : 0);
        ejecucion.setDuracionMs(request.duracionMs());
        ejecucion.setFinalizadoEn(request.finalizadoEn());
        ejecucion.setResumen(request.resumen());
    }

    public static PipelineEjecucionResponse toResponse(PipelineEjecucion ejecucion) {
        Pipeline pipeline = ejecucion.getPipeline();
        SolicitudCompra solicitud = ejecucion.getSolicitudCompra();
        return new PipelineEjecucionResponse(
                ejecucion.getId(),
                pipeline.getId(),
                pipeline.getNombre(),
                solicitud != null ? solicitud.getId() : null,
                solicitud != null ? solicitud.getTitulo() : null,
                ejecucion.getEstado(),
                ejecucion.getRegistrosProcesados(),
                ejecucion.getErroresEncontrados(),
                ejecucion.getDuracionMs(),
                ejecucion.getIniciadoEn(),
                ejecucion.getFinalizadoEn(),
                ejecucion.getResumen()
        );
    }
}
