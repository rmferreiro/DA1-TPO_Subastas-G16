package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad NUEVA — Multas para usuarios que no pagan sus pujas ganadas.
 * Multa = 10% del valor ofertado. Debe pagarse antes de participar nuevamente.
 * Si no cumple en 72hs, se deriva a la justicia y se bloquea al usuario.
 */
@Entity
@Table(name = "multas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta")
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private ItemCatalogo item;

    @Column(name = "monto_ofertado", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoOfertado;

    @Column(name = "monto_multa", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoMulta;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pagada = false;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Builder.Default
    @Column(name = "derivado_justicia", nullable = false)
    private Boolean derivadoJusticia = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
