package cl.duoc.nexora.backend.model;

import cl.duoc.nexora.backend.enums.AuthProvider;
import cl.duoc.nexora.backend.enums.RolUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Column(nullable = false)
    private Boolean activo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AuthProvider provider;

    @Column(length = 120)
    private String providerId;

    @Column(length = 500)
    private String fotoUrl;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "actualizado_en", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (rol == null) {
            rol = RolUsuario.COMPRADOR;
        }
        if (activo == null) {
            activo = Boolean.TRUE;
        }
    }

    public LocalDateTime getCreadoEn() {
        return createdAt;
    }

    public LocalDateTime getActualizadoEn() {
        return updatedAt;
    }

}
