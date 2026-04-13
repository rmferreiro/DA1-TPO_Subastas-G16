package ar.edu.uade.grupo16.subastas.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponse {
    private Long id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private boolean leida;
    private Long referenciaId;
    private String referenciaTipo;
    private LocalDateTime fechaCreacion;
}
