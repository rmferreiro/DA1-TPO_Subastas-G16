package tpo.g16.blackwood.network.models;

public class MiPuja {
    private int itemId;
    private String productoDesc;
    private int subastaId;
    private String subastaDesc;
    private double miPuja;
    private double pujaActual;
    private String estado;
    private String subastado;

    public int getItemId() { return itemId; }
    public String getProductoDesc() { return productoDesc; }
    public int getSubastaId() { return subastaId; }
    public String getSubastaDesc() { return subastaDesc; }
    public double getMiPuja() { return miPuja; }
    public double getPujaActual() { return pujaActual; }
    public String getEstado() { return estado; }
    public String getSubastado() { return subastado; }
}
