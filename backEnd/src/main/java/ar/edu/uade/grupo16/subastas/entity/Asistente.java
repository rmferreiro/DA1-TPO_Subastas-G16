package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asistentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @Column(name = "numeroPostor", nullable = false)
    private Integer numeroPostor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta", nullable = false)
    private Subasta subasta;
}
