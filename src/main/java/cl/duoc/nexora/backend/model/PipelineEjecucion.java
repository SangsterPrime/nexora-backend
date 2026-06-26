package cl.duoc.nexora.backend.model;

import cl.duoc.nexora.backend.enums.EstadoPipelineEjecucion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pipeline_ejecuciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineEjecucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_compra_id")
    private SolicitudCompra solicitudCompra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPipelineEjecucion estado;

    private Integer registrosProcesados;

    private Integer erroresEncontrados;

    private Long duracionMs;

    @Column(nullable = false, updatable = false)
    private LocalDateTime iniciadoEn;

    private LocalDateTime finalizadoEn;

    @Column(length = 1000)
    private String resumen;

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoPipelineEjecucion.PENDIENTE;
        }
        if (registrosProcesados == null) {
            registrosProcesados = 0;
        }
        if (erroresEncontrados == null) {
            erroresEncontrados = 0;
        }
        if (iniciadoEn == null) {
            iniciadoEn = LocalDateTime.now();
        }
    }

}
