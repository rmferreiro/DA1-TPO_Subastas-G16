package tpo.g16.blackwood.network.models;

public class PujaResponse {
    private int pujoId;
    private int itemId;
    private String nombrePostor;
    private int numeroPostor;
    private double importe;
    private double precioBase;
    private Double mejorOfertaAnterior;
    private String fechaHora;
    private boolean esGanadora;
    private String mensaje;

    public int getPujoId() { return pujoId; }
    public int getItemId() { return itemId; }
    public String getNombrePostor() { return nombrePostor; }
    public int getNumeroPostor() { return numeroPostor; }
    public double getImporte() { return importe; }
    public double getPrecioBase() { return precioBase; }
    public Double getMejorOfertaAnterior() { return mejorOfertaAnterior; }
    public String getFechaHora() { return fechaHora; }
    public boolean isEsGanadora() { return esGanadora; }
    public String getMensaje() { return mensaje; }
}
