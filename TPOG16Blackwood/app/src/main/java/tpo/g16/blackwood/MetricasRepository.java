package tpo.g16.blackwood;

import java.util.ArrayList;
import java.util.List;

/**
 * Punto único de acceso a los datos de métricas (Panel de Control,
 * Subastas en vivo, Control de pujas).
 *
 * HOY: valores de ejemplo en memoria, los mismos que ya estaban hardcodeados
 * en los layouts originales.
 *
 * CUANDO SE CONECTE EL BACKEND: este es el ÚNICO archivo que hay que tocar.
 * - obtenerOfertasEnVivo() -> GET /api/metricas/ofertas-en-vivo
 * - obtenerEstadoSistema() -> GET /api/metricas/estado-sistema
 * - obtenerItemsEnVivo()   -> GET /api/subastas/en-vivo (o vía WebSocket, según
 *                             el enunciado del TPO, ya que pide actualización
 *                             en tiempo real de las ofertas)
 * - obtenerLogsPujas()     -> GET /api/pujas/recientes (también candidato a
 *                             WebSocket para que se actualice solo)
 * "Subastas activas" NO está acá: se calcula directamente desde
 * SubastaRepository.contarEnSala(), porque es un dato derivado de las
 * subastas reales y no tiene sentido duplicarlo.
 */
public class MetricasRepository {

    private static MetricasRepository instancia;

    private int ofertasEnVivo = 124;
    private String estadoSistema = "Operando con normalidad";

    private final List<ItemEnVivo> itemsEnVivo = new ArrayList<>();
    private final List<LogPuja> logsPujas = new ArrayList<>();
    private final List<LogOferta> logsOfertas = new ArrayList<>();

    private MetricasRepository() {
        itemsEnVivo.add(new ItemEnVivo("Escultura moderna", "$2.400", 12));
        itemsEnVivo.add(new ItemEnVivo("Vino colección", "$900", 5));

        logsPujas.add(new LogPuja("Usuario123", "1000"));
        logsPujas.add(new LogPuja("Usuario456", "1200"));
        logsPujas.add(new LogPuja("Usuario123", "1300"));

        logsOfertas.add(new LogOferta("M. Rodríguez", "$3.200", LogOferta.LIDER));
        logsOfertas.add(new LogOferta("Juan Pérez", "$3.150", LogOferta.SUPERADA));
        logsOfertas.add(new LogOferta("Ana López", "$3.120", LogOferta.SUPERADA));
        logsOfertas.add(new LogOferta("R. Gómez", "$3.100", LogOferta.SUPERADA));
        logsOfertas.add(new LogOferta("L. Torres", "$2.900", LogOferta.RECHAZADA));
    }

    public static synchronized MetricasRepository getInstance() {
        if (instancia == null) {
            instancia = new MetricasRepository();
        }
        return instancia;
    }

    /** TODO (backend): reemplazar por GET /api/metricas/ofertas-en-vivo. */
    public int obtenerOfertasEnVivo() {
        return ofertasEnVivo;
    }

    /** TODO (backend): reemplazar por GET /api/metricas/estado-sistema. */
    public String obtenerEstadoSistema() {
        return estadoSistema;
    }

    /** TODO (backend / WebSocket): reemplazar por GET /api/subastas/en-vivo. */
    public List<ItemEnVivo> obtenerItemsEnVivo() {
        return itemsEnVivo;
    }

    /** TODO (backend / WebSocket): reemplazar por GET /api/pujas/recientes. */
    public List<LogPuja> obtenerLogsPujas() {
        return logsPujas;
    }

    /** TODO (backend / WebSocket): reemplazar por GET /api/subastas/{id}/ofertas. */
    public List<LogOferta> obtenerLogsOfertas() {
        return logsOfertas;
    }
}
