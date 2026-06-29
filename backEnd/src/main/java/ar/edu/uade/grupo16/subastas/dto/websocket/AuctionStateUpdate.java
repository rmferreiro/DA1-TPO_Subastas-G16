package ar.edu.uade.grupo16.subastas.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionStateUpdate {
    private double currentPrice;
    private String topBidderName;
    private long endEpochMillis;      // epoch en millis del momento en que vence el lote
    private int lotNumber;
}
