package ar.edu.uade.grupo16.subastas.entity;

import ar.edu.uade.grupo16.subastas.enums.Moneda;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "subastas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "estado", length = 10)
    private String estado; // 'abierta' o 'cerrada' (Legacy: 'carrada' con typo)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subastador")
    private Subastador subastador;

    @Column(name = "ubicacion", length = 350)
    private String ubicacion;

    @Column(name = "capacidadAsistentes")
    private Integer capacidadAsistentes;

    @Column(name = "tieneDeposito", length = 2)
    private String tieneDeposito; // 'si' o 'no'

    @Column(name = "seguridadPropia", length = 2)
    private String seguridadPropia; // 'si' o 'no'

    @Column(name = "categoria", length = 10)
    private String categoria; // comun, especial, plata, oro, platino

    // ---- Campos nuevos (no existen en Legacy) ----

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", length = 3)
    private Moneda moneda; // ARS o USD

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    // Item que se está subastando actualmente (para tracking en vivo)
    @Column(name = "item_actual_id")
    private Integer itemActualId;
}
