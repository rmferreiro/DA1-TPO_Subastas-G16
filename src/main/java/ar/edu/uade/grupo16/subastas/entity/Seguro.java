package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seguros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguro {

    @Id
    @Column(name = "numero_poliza", length = 50)
    private String numeroPoliza;

    @Column(name = "compania", length = 200)
    private String compania;

    @Column(name = "monto_cubierto", precision = 18, scale = 2)
    private java.math.BigDecimal montoCubierto;

    @Builder.Default
    @Column(name = "vigente", nullable = false)
    private Boolean vigente = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto")
    private Producto producto;
}
