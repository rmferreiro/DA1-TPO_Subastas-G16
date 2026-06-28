package tpo.g16.blackwood.network.models;

public class SubastaResponse {
    private int id;
    private String fecha;
    private String hora;
    private String estado;
    private String categoria;
    private String ubicacion;
    private String moneda;
    private String descripcion;
    private String subastadorNombre;
    private int capacidadAsistentes;
    private int asistentesActuales;
    private boolean tieneDeposito;
    private boolean seguridadPropia;

    public SubastaResponse() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSubastadorNombre() { return subastadorNombre; }
    public void setSubastadorNombre(String subastadorNombre) { this.subastadorNombre = subastadorNombre; }

    public int getCapacidadAsistentes() { return capacidadAsistentes; }
    public void setCapacidadAsistentes(int capacidadAsistentes) { this.capacidadAsistentes = capacidadAsistentes; }

    public int getAsistentesActuales() { return asistentesActuales; }
    public void setAsistentesActuales(int asistentesActuales) { this.asistentesActuales = asistentesActuales; }

    public boolean isTieneDeposito() { return tieneDeposito; }
    public void setTieneDeposito(boolean tieneDeposito) { this.tieneDeposito = tieneDeposito; }

    public boolean isSeguridadPropia() { return seguridadPropia; }
    public void setSeguridadPropia(boolean seguridadPropia) { this.seguridadPropia = seguridadPropia; }
}
