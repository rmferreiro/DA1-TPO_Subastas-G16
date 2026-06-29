package tpo.g16.blackwood.network.models;

import java.util.List;

public class MiPuja {
    private int itemId;
    private String productoDesc;
    private int subastaId;
    private String subastaDesc;
    private double miPuja;
    private double pujaActual;
    private String estado;
    private String subastado;
    private List<Double> todasMisPujas;
    private double comision;
    private double costoEnvio;
    private double totalAPagar;

    public int getItemId() { return itemId; }
    public String getProductoDesc() { return productoDesc; }
    public int getSubastaId() { return subastaId; }
    public String getSubastaDesc() { return subastaDesc; }
    public double getMiPuja() { return miPuja; }
    public double getPujaActual() { return pujaActual; }
    public String getEstado() { return estado; }
    public String getSubastado() { return subastado; }
    public List<Double> getTodasMisPujas() { return todasMisPujas; }
    public double getComision() { return comision; }
    public double getCostoEnvio() { return costoEnvio; }
    public double getTotalAPagar() { return totalAPagar; }
}
