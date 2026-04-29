package ar.edu.uade.grupo16.subastas.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PujaResponse {
    private Integer pujoId;
    private Integer itemId;
    private String nombrePostor;
    private Integer numeroPostor;
    private BigDecimal importe;
    private BigDecimal precioBase;
    private BigDecimal mejorOfertaAnterior;
    private LocalDateTime fechaHora;
    private boolean esGanadora;
    private String mensaje;
}
