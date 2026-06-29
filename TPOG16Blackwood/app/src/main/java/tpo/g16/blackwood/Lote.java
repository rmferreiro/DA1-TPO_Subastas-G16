package tpo.g16.blackwood;

/**
 * Modelo de un lote propuesto por un usuario para subasta.
 * Representa lo mismo que después va a ir y volver del backend:
 * GET /api/lotes (listar), PUT /api/lotes/{id} (actualizar estado/ubicación/póliza).
 */
public class Lote {

    private final int id;
    private String nombre;
    private String duenio;
    private String valorEstimado;

    private EstadoLote estado;
    private String motivoRechazo = "";
    private String valorBasePropuesto = "";
    private String comisionPropuesta = "";
    private String ubicacion = "";
    private String poliza = "";

    public Lote(int id, String nombre, String duenio, String valorEstimado, EstadoLote estado) {
        this.id = id;
        this.nombre = nombre;
        this.duenio = duenio;
        this.valorEstimado = valorEstimado;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDuenio() { return duenio; }
    public String getValorEstimado() { return valorEstimado; }

    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getValorBasePropuesto() { return valorBasePropuesto; }
    public void setValorBasePropuesto(String valorBasePropuesto) { this.valorBasePropuesto = valorBasePropuesto; }

    public String getComisionPropuesta() { return comisionPropuesta; }
    public void setComisionPropuesta(String comisionPropuesta) { this.comisionPropuesta = comisionPropuesta; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getPoliza() { return poliza; }
    public void setPoliza(String poliza) { this.poliza = poliza; }
}
