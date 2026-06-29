package tpo.g16.blackwood.network.models.websocket;

public class AuctionStateUpdate {
    public double currentPrice;
    public String topBidderName;
    public long endEpochMillis;
    public int lotNumber;
}
