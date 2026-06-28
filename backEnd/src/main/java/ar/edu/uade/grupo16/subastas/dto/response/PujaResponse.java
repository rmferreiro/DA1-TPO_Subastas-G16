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
    /** Próxima puja mínima válida (mejor oferta actual + 1% del precio base). */
    private BigDecimal siguientePujaMinima;
    /** Próxima puja máxima válida (mejor oferta actual + 20% del precio base). null para Oro/Platino. */
    private BigDecimal siguientePujaMaxima;
    /** true si la subasta es Oro o Platino (sin límite superior de puja). */
    private boolean sinLimiteMaximo;
}

