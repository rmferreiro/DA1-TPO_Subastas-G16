package tpo.g16.blackwood;

/**
 * Un ítem destacado dentro del catálogo de una subasta (lo que se muestra
 * en "Lotes destacados" dentro del Detalle de Subasta).
 */
public class LoteDestacado {

    private final String numero;       // "Lote #001"
    private final String nombre;       // "Reloj Patek Philippe vintage"
    private final String estimacion;   // "Est. 2.500 – 4.000 USD"

    public LoteDestacado(String numero, String nombre, String estimacion) {
        this.numero = numero;
        this.nombre = nombre;
        this.estimacion = estimacion;
    }

    public String getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public String getEstimacion() { return estimacion; }
}
