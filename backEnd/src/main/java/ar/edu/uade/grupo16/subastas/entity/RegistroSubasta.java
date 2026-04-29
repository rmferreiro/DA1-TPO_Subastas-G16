package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "registroDeSubasta")
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
}
