package tpo.g16.blackwood;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de una subasta.
 * Representa exactamente lo que viene del backend (GET /subastas, GET /subastas/{id})
 * y lo que hay que mandar al crear una nueva (POST /subastas).
 */
public class Subasta {

    // Mismos valores que EmpleadoDetalleSubastaActivity.NO_INICIADA / EN_PROCESO / TERMINADA
    public static final int PROXIMA = EmpleadoDetalleSubastaActivity.NO_INICIADA;
    public static final int EN_SALA = EmpleadoDetalleSubastaActivity.EN_PROCESO;
    public static final int FINALIZADA = EmpleadoDetalleSubastaActivity.TERMINADA;

    private final int id;
    private int estado;          // PROXIMA / EN_SALA / FINALIZADA
    private LocalDate fecha;     // fecha real, no texto suelto
    private String hora;         // "19:00"
    private String ciudad;
    private String sala;
    private String rematador;
    private String categoria;    // Común, Especial, Plata, Oro, Platino
    private String estimacion;   // estimación inicial en USD, como texto para mostrar directo
    private String incrementoMinimo = "10"; // USD, valor por defecto si no se especifica
    private String cantidadLotes;
    private List<LoteDestacado> lotesDestacados = new ArrayList<>();

    public Subasta(int id, int estado, LocalDate fecha, String hora, String ciudad, String sala,
                    String rematador, String categoria, String estimacion, String cantidadLotes) {
        this.id = id;
        this.estado = estado;
        this.fecha = fecha;
        this.hora = hora;
        this.ciudad = ciudad;
        this.sala = sala;
        this.rematador = rematador;
        this.categoria = categoria;
        this.estimacion = estimacion;
        this.cantidadLotes = cantidadLotes;
    }

    public int getId() { return id; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    public LocalDate getFecha() { return fecha; }
    /** Fecha lista para mostrar en pantalla, ej. "27 junio 2026". */
    public String getFechaFormateada() { return FechaUtils.formatear(fecha); }

    public String getHora() { return hora; }
    public String getCiudad() { return ciudad; }
    public String getSala() { return sala; }
    public String getRematador() { return rematador; }
    public String getCategoria() { return categoria; }
    public String getEstimacion() { return estimacion; }
    public String getCantidadLotes() { return cantidadLotes; }

    public String getIncrementoMinimo() { return incrementoMinimo; }
    public void setIncrementoMinimo(String incrementoMinimo) { this.incrementoMinimo = incrementoMinimo; }

    public List<LoteDestacado> getLotesDestacados() { return lotesDestacados; }
    public void setLotesDestacados(List<LoteDestacado> lotesDestacados) { this.lotesDestacados = lotesDestacados; }

    public String getEtiquetaEstado() {
        switch (estado) {
            case PROXIMA: return "● Próxima";
            case EN_SALA: return "● En sala";
            case FINALIZADA: return "● Finalizada";
            default: return "";
        }
    }
}
