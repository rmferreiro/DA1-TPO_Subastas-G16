package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrosSubasta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta", nullable = false)
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duenio", nullable = false)
    private Duenio duenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "importe", nullable = false, precision = 18, scale = 2)
    private BigDecimal importe;

    @Column(name = "comision", nullable = false, precision = 18, scale = 2)
    private BigDecimal comision;

    @Column(name = "pagado")
    private Boolean pagado = false;

    @Column(name = "costo_envio", precision = 18, scale = 2)
    private BigDecimal costoEnvio;

    /**
     * true si nadie pujó y la empresa compró el item al precio base.
     * false en el caso normal (compra por un postor).
     */
    @Builder.Default
    @Column(name = "compra_empresa", nullable = false)
    private Boolean compraEmpresa = false;

    // Fecha en que se cerró el lote — usada para el timer de 30 min de pago
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
