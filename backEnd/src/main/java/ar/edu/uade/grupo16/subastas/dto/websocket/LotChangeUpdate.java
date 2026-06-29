package ar.edu.uade.grupo16.subastas.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotChangeUpdate {
    private int soldLotNumber;
    private int soldLotOrder;        // orden de presentación del lote vendido
    private String soldLotWinnerName;
    private double soldLotFinalPrice;
    // Datos del nuevo lote activo:
    private int newLotNumber;        // ID interno del item (para tracking)
    private int newLotOrder;         // orden de presentación (1, 2, 3…) para mostrar en UI
    private String newLotTitle;
    private String newLotDescription;
    private String newLotImageUrl;
    private double newLotStartingPrice;  // precio postor base (Blackwood)
    private String newLotStartingBidder; // "Blackwood"
    private long newLotEndEpochMillis;   // epoch de fin del nuevo lote
}
