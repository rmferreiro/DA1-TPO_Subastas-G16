package tpo.g16.blackwood;

import java.util.ArrayList;
import java.util.List;

/**
 * Punto único de acceso a los datos de lotes (bienes propuestos para subasta).
 *
 * HOY: guarda todo en memoria, con los 3 lotes de ejemplo que ya estaban
 * hardcodeados en el diseño original (cada uno en un estado distinto, para
 * mostrar la trazabilidad completa del flujo).
 *
 * CUANDO SE CONECTE EL BACKEND: este es el ÚNICO archivo que hay que tocar.
 * - obtenerTodos()      -> reemplazar por un GET /api/lotes
 * - obtenerPorId(id)    -> reemplazar por un GET /api/lotes/{id}
 * - actualizar(Lote)    -> reemplazar por un PUT /api/lotes/{id}
 * Ninguna pantalla (Activity/XML) necesita cambios: todas leen y escriben
 * a través de estos métodos.
 */
public class LoteRepository {

    private static LoteRepository instancia;

    private final List<Lote> lotes = new ArrayList<>();

    private LoteRepository() {
        lotes.add(new Lote(1, "Reloj Patek Philippe 1950s", "Carlos Méndez", "15.000",
                EstadoLote.PENDIENTE_INSPECCION));
        lotes.add(new Lote(2, "Vajilla de plata Sterling", "Ana López", "8.000",
                EstadoLote.PROPUESTA_ENVIADA));
        lotes.add(new Lote(3, "Cuadro Berni - Escuela Rioplatense", "Roberto Silva", "22.000",
                EstadoLote.INCLUIDO_SUBASTA));
    }

    public static synchronized LoteRepository getInstance() {
        if (instancia == null) {
            instancia = new LoteRepository();
        }
        return instancia;
    }

    /**
     * TODO (backend): reemplazar por GET /api/lotes y mapear la respuesta a List<Lote>.
     */
    public List<Lote> obtenerTodos() {
        return lotes;
    }

    /**
     * TODO (backend): reemplazar por GET /api/lotes/{id}.
     */
    public Lote obtenerPorId(int id) {
        for (Lote lote : lotes) {
            if (lote.getId() == id) return lote;
        }
        return null;
    }

    /**
     * TODO (backend): reemplazar por PUT /api/lotes/{id} enviando los campos actualizados.
     * Mientras no haya backend, como Lote ya es el mismo objeto en memoria, no hace falta
     * hacer nada más que llamar a este método para "confirmar" el guardado (queda el lugar
     * marcado para cuando haya una llamada real).
     */
    public void actualizar(Lote lote) {
        // No-op por ahora: los setters de Lote ya modificaron el objeto en memoria.
    }
}
