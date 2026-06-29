package ar.edu.uade.grupo16.subastas.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {
    private double amount;
    /** ID del medio de pago que el cliente validó al entrar a la subasta (opcional). */
    private Long medioPagoId;
}
