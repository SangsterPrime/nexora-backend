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
 * Resultado scoreado por el modelo de IA, escrito por el Render Cron Job de
 * {@code EntrenamientoAI} en Neon. El backend solo lo <strong>lee</strong>
 * (modo {@code CRON}).
 */
@Entity
@Table(name = "ml_predicciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlPrediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Marca temporal de la predicción (se ordena por este campo, {@code ts DESC}). */
    @Column(nullable = false)
    private LocalDateTime ts;

    /** Tipo de entidad evaluada, p. ej. {@code "COTIZACION"} (opcional). */
    @Column(length = 60)
    private String entidad;

    /** ID de la entidad de negocio asociada (opcional). */
    @Column(name = "entidad_id")
    private Long entidadId;

    private Double score;

    private Double probabilidad;

    /** Etiqueta o clase predicha (opcional). */
    @Column(length = 120)
    private String prediccion;
}
