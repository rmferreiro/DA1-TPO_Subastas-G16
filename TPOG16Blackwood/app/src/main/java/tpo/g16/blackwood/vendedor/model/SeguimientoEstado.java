package tpo.g16.blackwood.vendedor.model;

public class SeguimientoEstado {
    private EstadoLote estado;
    private String fecha;
    private String descripcion;

    public SeguimientoEstado(EstadoLote estado, String fecha, String descripcion) {
        this.estado = estado;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}