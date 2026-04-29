package ar.edu.uade.grupo16.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fotos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identificador")
    private Integer identificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto", nullable = false)
    private Producto producto;

    @Lob
    @Column(name = "foto", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] foto;
}
