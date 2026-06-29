package tpo.g16.blackwood.network.models.websocket;

public class BidRequest {
    public double amount;
    /** Medio de pago validado al entrar a la subasta (opcional). */
    public Long medioPagoId;
}
