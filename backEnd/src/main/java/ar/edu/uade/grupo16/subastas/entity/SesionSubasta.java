package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad NUEVA — Controla que un usuario solo pueda estar
 * conectado a UNA sola subasta activa a la vez.
 * UNIQUE en cliente_id garantiza esta restricción.
 */
@Entity
@Table(name = "sesiones_subasta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = false)
    private Subasta subasta;

    @Column(name = "fecha_conexion")
    private LocalDateTime fechaConexion;
}
