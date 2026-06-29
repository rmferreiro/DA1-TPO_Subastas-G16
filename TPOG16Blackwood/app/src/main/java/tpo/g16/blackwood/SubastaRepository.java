package tpo.g16.blackwood;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Punto único de acceso a los datos de subastas.
 *
 * HOY: guarda todo en memoria (se pierde al cerrar la app), con las 2 subastas
 * de ejemplo que ya estaban hardcodeadas en el diseño original.
 *
 * CUANDO SE CONECTE EL BACKEND: este es el ÚNICO archivo que hay que tocar.
 * - obtenerTodas()      -> reemplazar por un GET /api/subastas
 * - obtenerPorId(id)    -> reemplazar por un GET /api/subastas/{id}
 * - crear(Subasta)      -> reemplazar por un POST /api/subastas
 * - contarEnSala()      -> puede seguir calculándose local a partir de obtenerTodas(),
 *                          o reemplazarse por un campo que ya venga calculado del backend.
 * Ninguna pantalla (Activity/XML) necesita cambios: todas leen y escriben
 * a través de estos métodos.
 */
public class SubastaRepository {

    private static SubastaRepository instancia;

    private final List<Subasta> subastas = new ArrayList<>();
    private int proximoId = 1;

    private SubastaRepository() {
        Subasta s1 = new Subasta(
                proximoId++, Subasta.PROXIMA, LocalDate.of(2026, 3, 25), "19:00",
                "Buenos Aires", "Sala Norte", "Ruiz",
                "Platino", "15.000", "18"
        );

        Subasta s2 = new Subasta(
                proximoId++, Subasta.EN_SALA, LocalDate.of(2026, 3, 20), "18:00",
                "Mendoza", "Sala Central", "López",
                "Oro", "50.000", "40"
        );
        // Datos de ejemplo que ya estaban hardcodeados en activity_empleado_detalle_subasta.xml
        s2.setIncrementoMinimo("10");
        s2.setLotesDestacados(Arrays.asList(
                new LoteDestacado("Lote #001", "Reloj Patek Philippe vintage", "Est. 2.500 – 4.000 USD"),
                new LoteDestacado("Lote #002", "Vajilla de plata Sterling", "Est. 1.200 – 2.000 USD"),
                new LoteDestacado("Lote #003", "Cuadro óleo – Escuela Rioplatense", "Est. 800 – 1.500 USD")
        ));

        subastas.add(s1);
        subastas.add(s2);
    }

    public static synchronized SubastaRepository getInstance() {
        if (instancia == null) {
            instancia = new SubastaRepository();
        }
        return instancia;
    }

    /**
     * TODO (backend): reemplazar por GET /api/subastas y mapear la respuesta a List<Subasta>.
     */
    public List<Subasta> obtenerTodas() {
        return subastas;
    }

    /**
     * TODO (backend): reemplazar por GET /api/subastas/{id}.
     */
    public Subasta obtenerPorId(int id) {
        for (Subasta s : subastas) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    /**
     * TODO (backend): reemplazar por POST /api/subastas enviando los mismos campos.
     * Mientras no haya backend, agrega la subasta a la lista en memoria con un id nuevo.
     */
    public void crear(Subasta nueva) {
        subastas.add(0, nueva); // la agrega arriba de todo para verla primero en la lista
    }

    /**
     * Cantidad de subastas actualmente "en sala". Se usa en el Panel de Control.
     * Hoy se calcula localmente a partir de obtenerTodas(); si el backend ya expone
     * este número calculado, se puede reemplazar directamente acá.
     */
    public int contarEnSala() {
        int cantidad = 0;
        for (Subasta s : subastas) {
            if (s.getEstado() == Subasta.EN_SALA) cantidad++;
        }
        return cantidad;
    }

    /**
     * Genera el próximo id disponible. Lo usa EmpleadoCrearSubastaActivity al crear
     * una subasta nueva en memoria; cuando haya backend, el id lo va a asignar el
     * servidor y este método deja de ser necesario.
     */
    public int generarProximoId() {
        return proximoId++;
    }
}
