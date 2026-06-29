package tpo.g16.blackwood;

/** Una línea del log de pujas en tiempo real (Control de pujas). */
public class LogPuja {

    private final String usuario;
    private final String monto;

    public LogPuja(String usuario, String monto) {
        this.usuario = usuario;
        this.monto = monto;
    }

    public String getUsuario() { return usuario; }
    public String getMonto() { return monto; }

    public String getTexto() {
        return usuario + " ofertó $" + monto;
    }
}
