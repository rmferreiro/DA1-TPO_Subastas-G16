package tpo.g16.blackwood;

/** Un ítem que se está subastando en vivo en este momento. */
public class ItemEnVivo {

    private final String nombre;
    private final String montoActual;     // "$2.400"
    private final int cantidadOfertas;

    public ItemEnVivo(String nombre, String montoActual, int cantidadOfertas) {
        this.nombre = nombre;
        this.montoActual = montoActual;
        this.cantidadOfertas = cantidadOfertas;
    }

    public String getNombre() { return nombre; }
    public String getMontoActual() { return montoActual; }
    public int getCantidadOfertas() { return cantidadOfertas; }
}
