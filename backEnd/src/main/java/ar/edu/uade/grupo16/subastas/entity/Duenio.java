package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "duenios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Duenio {

    @Id
    @Column(name = "identificador")
    private Integer identificador;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "identificador")
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numeroPais")
    private Pais pais;

    @Column(name = "verificacionFinanciera", length = 2)
    private String verificacionFinanciera; // 'si' o 'no'

    @Column(name = "verificacionJudicial", length = 2)
    private String verificacionJudicial; // 'si' o 'no'

    @Column(name = "calificacionRiesgo")
    private Integer calificacionRiesgo; // 1 a 6

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificador", nullable = false)
    private Empleado verificador;
}
