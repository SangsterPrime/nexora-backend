package cl.duoc.nexora.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Métricas de una corrida del modelo de IA, escritas por el Render Cron Job de
 * {@code EntrenamientoAI} en Neon. El backend solo las <strong>lee</strong>
 * (modo {@code CRON}); por eso no define lógica de escritura ni {@code @PrePersist}.
 */
@Entity
@Table(name = "ml_metricas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlMetrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Marca temporal de la corrida (se ordena por este campo, {@code ts DESC}). */
    @Column(nullable = false)
    private LocalDateTime ts;

    private Double accuracy;

    private Double recall;

    @Column(name = "precision")
    private Double precision;

    private Double f1;

    @Column(name = "roc_auc")
    private Double rocAuc;

    private Double gini;

    /** Matriz de confusión serializada como JSON, p. ej. {@code [[10,2],[1,20]]}. */
    @Column(name = "matriz_confusion", columnDefinition = "text")
    private String matrizConfusion;

    /** Nombre o versión del modelo (opcional). */
    @Column(length = 120)
    private String modelo;

    /** Cantidad de registros usados en el entrenamiento (opcional). */
    @Column(name = "n_samples")
    private Integer nSamples;
}
