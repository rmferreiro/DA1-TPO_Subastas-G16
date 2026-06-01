package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "disponible", length = 2)
    private String disponible; // 'si' o 'no'

    @Column(name = "descripcionCatalogo", length = 500)
    private String descripcionCatalogo;

    @Column(name = "descripcionCompleta", nullable = false, length = 300)
    private String descripcionCompleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisor", nullable = false)
    private Empleado revisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duenio", nullable = false)
    private Duenio duenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguro")
    private Seguro seguro;

    // Campo nuevo para diferenciar tipos de producto
    @Column(name = "tipo_producto", length = 20)
    private String tipoProducto; // 'ESTANDAR' o 'OBRA_ARTE'

    // Declaración de propiedad legítima
    @Column(name = "declaracion_propiedad")
    private Boolean declaracionPropiedad;

    // Estado de revisión por la empresa
    @Column(name = "estado_revision", length = 20)
    private String estadoRevision; // 'PENDIENTE', 'ACEPTADO', 'RECHAZADO'

    // Motivo de rechazo (si aplica)
    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    // Ubicación en depósito
    @Column(name = "ubicacion_deposito", length = 200)
    private String ubicacionDeposito;
}
