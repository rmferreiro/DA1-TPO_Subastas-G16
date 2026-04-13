package ar.edu.uade.grupo16.subastas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull(message = "El país es obligatorio")
    private Integer paisId;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El password es obligatorio")
    private String password;

    // Las fotos del documento se envían como Base64 strings
    @NotBlank(message = "La foto del frente del documento es obligatoria")
    private String fotoDocFrente;

    @NotBlank(message = "La foto del dorso del documento es obligatoria")
    private String fotoDocDorso;
}
