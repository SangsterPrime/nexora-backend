package cl.duoc.nexora.backend.model;

import cl.duoc.nexora.backend.enums.EstadoNegociacion;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "negociaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negociacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(precision = 15, scale = 2)
    private BigDecimal montoOfertado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoNegociacion estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoNegociacion.ABIERTA;
        }
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

}
