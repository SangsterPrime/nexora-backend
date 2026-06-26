package cl.duoc.nexora.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes_scoreados")
@Getter
@Setter
@NoArgsConstructor
public class ClienteScoreado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer edad;

    @Column(name = "anos_cliente")
    private Integer anosCliente;

    @Column(name = "uso_datos_gb")
    private Double usoDatosGb;

    @Column(name = "llamadas_mes")
    private Integer llamadasMes;

    private Integer reclamos;

    @Column(name = "plan_premium")
    private Integer planPremium;

    private Integer abandona;

    @Column(name = "prob_abandono")
    private Double probAbandono;

    @Column(name = "segmento_riesgo")
    private String segmentoRiesgo;

    @Column(name = "accion_retencion")
    private String accionRetencion;

    @Column(name = "fecha_carga")
    private LocalDateTime fechaCarga;
}
