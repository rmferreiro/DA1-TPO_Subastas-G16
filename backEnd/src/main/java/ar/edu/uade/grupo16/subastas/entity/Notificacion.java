package ar.edu.uade.grupo16.subastas.entity;

import ar.edu.uade.grupo16.subastas.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad NUEVA — Notificaciones internas del sistema.
 * No existe en la estructura Legacy.
 */
@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(nullable = false)
    private Boolean leida = false;

    // ID de la entidad relacionada (subasta, producto, etc.)
    @Column(name = "referencia_id")
    private Long referenciaId;

    // Tipo de entidad referenciada (SUBASTA, PRODUCTO, PUJO, etc.)
    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
