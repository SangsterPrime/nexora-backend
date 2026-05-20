package cl.duoc.nexora.backend.mapper;

import cl.duoc.nexora.backend.dto.request.PipelineRequest;
import cl.duoc.nexora.backend.dto.response.PipelineResponse;
import cl.duoc.nexora.backend.model.Pipeline;

public final class PipelineMapper {

    private PipelineMapper() {
    }

    public static Pipeline toEntity(PipelineRequest request) {
        Pipeline pipeline = new Pipeline();
        pipeline.setNombre(request.nombre());
        pipeline.setDescripcion(request.descripcion());
        pipeline.setTipo(request.tipo());
        pipeline.setActivo(request.activo() != null ? request.activo() : Boolean.TRUE);
        return pipeline;
    }

    public static void updateEntity(Pipeline pipeline, PipelineRequest request) {
        pipeline.setNombre(request.nombre());
        pipeline.setDescripcion(request.descripcion());
        pipeline.setTipo(request.tipo());
        if (request.activo() != null) {
            pipeline.setActivo(request.activo());
        }
    }

    public static PipelineResponse toResponse(Pipeline pipeline) {
        return new PipelineResponse(
                pipeline.getId(),
                pipeline.getNombre(),
                pipeline.getDescripcion(),
                pipeline.getTipo(),
                pipeline.getActivo(),
                pipeline.getCreadoEn()
        );
    }
}
