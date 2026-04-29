package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pujos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asistente", nullable = false)
    private Asistente asistente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item", nullable = false)
    private ItemCatalogo item;

    @Column(name = "importe", nullable = false, precision = 18, scale = 2)
    private BigDecimal importe;

    @Column(name = "ganador", length = 2)
    private String ganador; // 'si' o 'no'

    // Campo nuevo: timestamp para ordenar pujas cronológicamente
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;
}
