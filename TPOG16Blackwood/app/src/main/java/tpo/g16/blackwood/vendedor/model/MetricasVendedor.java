package tpo.g16.blackwood.vendedor.model;

public class MetricasVendedor {
    private int lotesPublicados;
    private int lotesVendidos;
    private int lotesRechazados;
    private double totalGenerado;

    public MetricasVendedor(int lotesPublicados, int lotesVendidos, int lotesRechazados, double totalGenerado) {
        this.lotesPublicados = lotesPublicados;
        this.lotesVendidos = lotesVendidos;
        this.lotesRechazados = lotesRechazados;
        this.totalGenerado = totalGenerado;
    }

    public int getLotesPublicados() {
        return lotesPublicados;
    }

    public void setLotesPublicados(int lotesPublicados) {
        this.lotesPublicados = lotesPublicados;
    }

    public int getLotesVendidos() {
        return lotesVendidos;
    }

    public void setLotesVendidos(int lotesVendidos) {
        this.lotesVendidos = lotesVendidos;
    }

    public int getLotesRechazados() {
        return lotesRechazados;
    }

    public void setLotesRechazados(int lotesRechazados) {
        this.lotesRechazados = lotesRechazados;
    }

    public double getTotalGenerado() {
        return totalGenerado;
    }

    public void setTotalGenerado(double totalGenerado) {
        this.totalGenerado = totalGenerado;
    }
}