package cl.duoc.nexora.backend.model;

import cl.duoc.nexora.backend.enums.TipoKpi;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kpi_resultados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_ejecucion_id", nullable = false)
    private PipelineEjecucion pipelineEjecucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoKpi tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate periodo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime calculadoEn;

    @PrePersist
    void prePersist() {
        if (calculadoEn == null) {
            calculadoEn = LocalDateTime.now();
        }
    }

}
