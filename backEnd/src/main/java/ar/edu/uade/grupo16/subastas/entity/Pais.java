package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "paises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero")
    private Integer numero;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @Column(name = "gentilicio", length = 100)
    private String gentilicio;

    @Column(name = "idiomas", nullable = false, length = 150)
    private String idiomas;
}
