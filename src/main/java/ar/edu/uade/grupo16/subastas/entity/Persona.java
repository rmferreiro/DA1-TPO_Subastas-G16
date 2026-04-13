package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @Column(name = "documento", nullable = false, length = 20)
    private String documento;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "direccion", length = 250)
    private String direccion;

    @Column(name = "estado", length = 15)
    private String estado; // 'activo' o 'inactivo'

    @Lob
    @Column(name = "foto", columnDefinition = "LONGBLOB")
    private byte[] foto;
}
