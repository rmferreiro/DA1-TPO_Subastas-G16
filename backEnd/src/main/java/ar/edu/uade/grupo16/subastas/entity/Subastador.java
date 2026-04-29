package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subastadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subastador {

    @Id
    @Column(name = "identificador")
    private Integer identificador;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "identificador")
    private Persona persona;

    @Column(name = "matricula", length = 15)
    private String matricula;

    @Column(name = "region", length = 50)
    private String region;
}
