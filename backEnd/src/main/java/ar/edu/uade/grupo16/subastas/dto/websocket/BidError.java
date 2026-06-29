package ar.edu.uade.grupo16.subastas.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidError {
    private String reason;
    private double minimumRequired; // precio actual + incremento mínimo
}
