package ar.edu.uade.grupo16.subastas.entity;

import ar.edu.uade.grupo16.subastas.enums.EstadoUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad NUEVA — Tabla de credenciales de autenticación.
 * No existe en la estructura Legacy. Vincula un email/password con una Persona
 * y controla el estado de aprobación del usuario.
 * El campo UUID se usa como identificador público para el endpoint de aprobación/rechazo.
 */
@Entity
@Table(name = "usuarios_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private Persona persona;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado;

    @Lob
    @Column(name = "foto_doc_frente", columnDefinition = "LONGBLOB")
    private byte[] fotoDocFrente;

    @Lob
    @Column(name = "foto_doc_dorso", columnDefinition = "LONGBLOB")
    private byte[] fotoDocDorso;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoUsuario.PENDIENTE;
        }
    }
}
