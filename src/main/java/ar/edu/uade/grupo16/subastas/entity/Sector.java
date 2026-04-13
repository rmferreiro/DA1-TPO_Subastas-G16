package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sectores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @Column(name = "nombreSector", nullable = false, length = 150)
    private String nombreSector;

    @Column(name = "codigoSector", length = 10)
    private String codigoSector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsableSector")
    private Empleado responsableSector;
}
