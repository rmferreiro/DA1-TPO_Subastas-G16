package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entidad NUEVA — Información adicional para productos que son obras de arte.
 * Tabla complementaria a 'productos', no reemplaza sino que extiende.
 */
@Entity
@Table(name = "productos_obra_arte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoObraArte {

    @Id
    @Column(name = "producto_id")
    private Integer productoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(length = 200)
    private String artista;

    @Column(length = 200)
    private String disenador;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String historia; // contexto, dueños anteriores, curiosidades
}
