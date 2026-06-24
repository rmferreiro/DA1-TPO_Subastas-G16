package tpo.g16.blackwood.network.models;

public class PujaRequest {
    private int itemId;
    private double importe;
    private Long medioPagoId;

    public PujaRequest(int itemId, double importe, Long medioPagoId) {
        this.itemId = itemId;
        this.importe = importe;
        this.medioPagoId = medioPagoId;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public Long getMedioPagoId() { return medioPagoId; }
    public void setMedioPagoId(Long medioPagoId) { this.medioPagoId = medioPagoId; }
}
