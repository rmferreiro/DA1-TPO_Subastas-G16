package tpo.g16.blackwood;

/** Una fila del detalle de ofertas dentro de "Logs de ofertas" (pantalla por subasta). */
public class LogOferta {

    public static final String LIDER = "Líder";
    public static final String SUPERADA = "Superada";
    public static final String RECHAZADA = "Rechazada";

    private final String usuario;
    private final String monto;
    private final String estado; // LIDER, SUPERADA o RECHAZADA

    public LogOferta(String usuario, String monto, String estado) {
        this.usuario = usuario;
        this.monto = monto;
        this.estado = estado;
    }

    public String getUsuario() { return usuario; }
    public String getMonto() { return monto; }
    public String getEstado() { return estado; }
}
