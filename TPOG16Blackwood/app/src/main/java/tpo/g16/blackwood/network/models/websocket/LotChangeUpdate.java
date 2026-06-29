package tpo.g16.blackwood.network.models.websocket;

public class LotChangeUpdate {
    public int soldLotNumber;
    public int soldLotOrder;         // orden de presentación del lote vendido
    public String soldLotWinnerName;
    public double soldLotFinalPrice;
    public int newLotNumber;         // ID interno del item
    public int newLotOrder;          // orden de presentación (1, 2, 3…) para mostrar en UI
    public String newLotTitle;
    public String newLotDescription;
    public String newLotImageUrl;
    public double newLotStartingPrice;
    public String newLotStartingBidder;
    public long newLotEndEpochMillis;
}
