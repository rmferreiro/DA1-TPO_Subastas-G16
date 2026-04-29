package ar.edu.uade.grupo16.subastas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobacionRequest {

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria; // comun, especial, plata, oro, platino
}
