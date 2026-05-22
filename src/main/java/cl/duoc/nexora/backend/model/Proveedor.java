package cl.duoc.nexora.backend.model;

import cl.duoc.nexora.backend.enums.EstadoProveedor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String rut;

    @Column(nullable = false, length = 160)
    private String razonSocial;

    @Column(length = 120)
    private String nombreContacto;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(length = 250)
    private String direccion;

    @Column(precision = 5, scale = 2)
    private BigDecimal reputacionScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal cumplimientoScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProveedor estado;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creadoEn;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoProveedor.ACTIVO;
        }
    }

}
