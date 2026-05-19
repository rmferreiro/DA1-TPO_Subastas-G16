package tpo.g16.blackwood.vendedor.model;

import java.util.ArrayList;
import java.util.List;

public class Lote {
    private int id;
    private String nombreArticulo;
    private String categoria;
    private String descripcion;
    private String estadoProducto;
    private double precioEstimado;
    private String fechaCreacion;
    private List<String> fotos;
    private EstadoLote estadoActual;
    private List<SeguimientoEstado> historialEstados;

    // Datos para solicitud aprobada
    private Double precioBase;
    private Double comision;
    private String fechaSubasta;

    // Datos para solicitud rechazada
    private String motivoRechazo;
    private Double costoDevolucion;
    private String observaciones;

    // Tasación
    private Double tasacion;

    public Lote(int id, String nombreArticulo, String categoria, String descripcion,
                String estadoProducto, double precioEstimado, String fechaCreacion,
                List<String> fotos, EstadoLote estadoActual) {
        this.id = id;
        this.nombreArticulo = nombreArticulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.estadoProducto = estadoProducto;
        this.precioEstimado = precioEstimado;
        this.fechaCreacion = fechaCreacion;
        this.fotos = fotos != null ? fotos : new ArrayList<>();
        this.estadoActual = estadoActual;
        this.historialEstados = new ArrayList<>();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombreArticulo() { return nombreArticulo; }
    public void setNombreArticulo(String nombreArticulo) { this.nombreArticulo = nombreArticulo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstadoProducto() { return estadoProducto; }
    public void setEstadoProducto(String estadoProducto) { this.estadoProducto = estadoProducto; }

    public double getPrecioEstimado() { return precioEstimado; }
    public void setPrecioEstimado(double precioEstimado) { this.precioEstimado = precioEstimado; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }

    public EstadoLote getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoLote estadoActual) { this.estadoActual = estadoActual; }

    public List<SeguimientoEstado> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<SeguimientoEstado> historialEstados) { this.historialEstados = historialEstados; }

    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }

    public Double getComision() { return comision; }
    public void setComision(Double comision) { this.comision = comision; }

    public String getFechaSubasta() { return fechaSubasta; }
    public void setFechaSubasta(String fechaSubasta) { this.fechaSubasta = fechaSubasta; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public Double getCostoDevolucion() { return costoDevolucion; }
    public void setCostoDevolucion(Double costoDevolucion) { this.costoDevolucion = costoDevolucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Double getTasacion() { return tasacion; }
    public void setTasacion(Double tasacion) { this.tasacion = tasacion; }

    public String getUltimaFecha() {
        if (historialEstados != null && !historialEstados.isEmpty()) {
            return historialEstados.get(historialEstados.size() - 1).getFecha();
        }
        return fechaCreacion;
    }

    public String getResumenEstado() {
        return estadoActual.getNombreVisual();
    }
}