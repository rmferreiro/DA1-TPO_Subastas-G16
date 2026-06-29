package tpo.g16.blackwood.vendedor.repository;

import java.util.ArrayList;
import java.util.List;

import tpo.g16.blackwood.vendedor.model.EstadoLote;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.model.MetricasVendedor;
import tpo.g16.blackwood.vendedor.model.SeguimientoEstado;

public class LoteRepository {

    private static LoteRepository instance;
    private final List<Lote> lotes;

    private LoteRepository() {
        lotes = new ArrayList<>();
        cargarDatosMock();
    }

    public static synchronized LoteRepository getInstance() {
        if (instance == null) {
            instance = new LoteRepository();
        }
        return instance;
    }

    private void cargarDatosMock() {
        // Lote 1: Aprobado y en subasta
        Lote lote1 = new Lote(1, "Juego de té de porcelana S.XVIII", "Antigüedades",
                "Elegante juego de té de porcelana inglesa del siglo XVIII, compuesto por 18 piezas en excelente estado de conservación.",
                "Excelente", 5000.0, "10/03/2026", new ArrayList<>(), EstadoLote.EN_SUBASTA);
        lote1.setPrecioBase(5500.0);
        lote1.setComision(12.0);
        lote1.setFechaSubasta("20/05/2026");
        lote1.setTasacion(5200.0);
        lote1.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_EN_PROCESO, "10/03/2026", "Solicitud recibida"));
        lote1.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.INICIO_TASACION, "12/03/2026", "Inicio del proceso de tasación"));
        lote1.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.EN_PROCESO, "15/03/2026", "En proceso de revisión y análisis"));
        lote1.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_APROBADA, "20/03/2026", "Solicitud aprobada. Tasación: $5.200"));
        lote1.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.EN_SUBASTA, "01/04/2026", "Incluido en subasta del 20/05/2026"));

        // Lote 2: En proceso
        Lote lote2 = new Lote(2, "Óleo abstracto - Martínez", "Arte",
                "Óleo sobre lienzo del artista contemporáneo Juan Martínez. Obra única con certificado de autenticidad.",
                "Bueno", 8000.0, "25/03/2026", new ArrayList<>(), EstadoLote.EN_PROCESO);
        lote2.setTasacion(7500.0);
        lote2.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_EN_PROCESO, "25/03/2026", "Solicitud recibida"));
        lote2.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.INICIO_TASACION, "28/03/2026", "Inicio del proceso de tasación"));
        lote2.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.EN_PROCESO, "01/04/2026", "En proceso de revisión por el comité de arte"));

        // Lote 3: Rechazado
        Lote lote3 = new Lote(3, "Reloj de bolsillo", "Coleccionables",
                "Reloj de bolsillo suizo antiguo con cadena de plata. Mecanismo de cuerda manual.",
                "Regular", 1500.0, "05/02/2026", new ArrayList<>(), EstadoLote.SOLICITUD_RECHAZADA);
        lote3.setMotivoRechazo("No se pudo verificar la autenticidad del mecanismo");
        lote3.setCostoDevolucion(250.0);
        lote3.setObservaciones("Se recomienda presentar certificado de autenticidad suizo.");
        lote3.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_EN_PROCESO, "05/02/2026", "Solicitud recibida"));
        lote3.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.INICIO_TASACION, "08/02/2026", "Inicio del proceso de tasación"));
        lote3.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_RECHAZADA, "15/02/2026", "Solicitud rechazada. Ver detalle."));

        // Lote 4: Comprado
        Lote lote4 = new Lote(4, "Anillo diamante 2ct", "Joyería",
                "Anillo de compromiso con diamante central de 2 quilates, engarzado en oro blanco 18K.",
                "Excelente", 12000.0, "15/01/2026", new ArrayList<>(), EstadoLote.LOTE_COMPRADO);
        lote4.setPrecioBase(12500.0);
        lote4.setComision(15.0);
        lote4.setFechaSubasta("15/03/2026");
        lote4.setTasacion(11800.0);
        lote4.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_EN_PROCESO, "15/01/2026", "Solicitud recibida"));
        lote4.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.INICIO_TASACION, "18/01/2026", "Inicio del proceso de tasación"));
        lote4.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.EN_PROCESO, "22/01/2026", "Verificación de autenticidad del diamante"));
        lote4.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_APROBADA, "01/02/2026", "Solicitud aprobada. Tasación: $11.800"));
        lote4.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.EN_SUBASTA, "15/03/2026", "Subastado el 15/03/2026"));
        lote4.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.LOTE_COMPRADO, "15/03/2026", "Vendido por $14.200"));

        // Lote 5: Solicitud en proceso (recién creado)
        Lote lote5 = new Lote(5, "Escultura bronce - Desnudo femenino", "Arte",
                "Escultura en bronce de 40cm de altura. Obra del escultor argentino Roberto Fernández.",
                "Bueno", 3500.0, "10/05/2026", new ArrayList<>(), EstadoLote.SOLICITUD_EN_PROCESO);
        lote5.getHistorialEstados().add(new SeguimientoEstado(EstadoLote.SOLICITUD_EN_PROCESO, "10/05/2026", "Solicitud recibida. Pendiente de revisión."));

        lotes.add(lote1);
        lotes.add(lote2);
        lotes.add(lote3);
        lotes.add(lote4);
        lotes.add(lote5);
    }

    public List<Lote> getLotes() {
        return new ArrayList<>(lotes);
    }

    public List<Lote> getLotesPorEstado(EstadoLote estado) {
        List<Lote> filtrados = new ArrayList<>();
        for (Lote lote : lotes) {
            if (lote.getEstadoActual() == estado) {
                filtrados.add(lote);
            }
        }
        return filtrados;
    }

    public List<Lote> getLotesPorFiltro(String filtro) {
        List<Lote> filtrados = new ArrayList<>();
        for (Lote lote : lotes) {
            switch (filtro.toLowerCase()) {
                case "en proceso":
                    if (lote.getEstadoActual() == EstadoLote.SOLICITUD_EN_PROCESO ||
                        lote.getEstadoActual() == EstadoLote.INICIO_TASACION ||
                        lote.getEstadoActual() == EstadoLote.EN_PROCESO) {
                        filtrados.add(lote);
                    }
                    break;
                case "aprobados":
                    if (lote.getEstadoActual() == EstadoLote.SOLICITUD_APROBADA ||
                        lote.getEstadoActual() == EstadoLote.EN_SUBASTA) {
                        filtrados.add(lote);
                    }
                    break;
                case "rechazados":
                    if (lote.getEstadoActual() == EstadoLote.SOLICITUD_RECHAZADA) {
                        filtrados.add(lote);
                    }
                    break;
                case "vendidos":
                    if (lote.getEstadoActual() == EstadoLote.LOTE_COMPRADO) {
                        filtrados.add(lote);
                    }
                    break;
            }
        }
        return filtrados;
    }

    public Lote getLotePorId(int id) {
        for (Lote lote : lotes) {
            if (lote.getId() == id) {
                return lote;
            }
        }
        return null;
    }

    public void agregarLote(Lote lote) {
        lote.setId(lotes.size() + 1);
        lotes.add(lote);
    }

    public MetricasVendedor getMetricas() {
        int publicados = 0;
        int vendidos = 0;
        int rechazados = 0;
        double totalGenerado = 0.0;

        for (Lote lote : lotes) {
            publicados++;
            if (lote.getEstadoActual() == EstadoLote.LOTE_COMPRADO) {
                vendidos++;
                totalGenerado += lote.getPrecioBase() != null ? lote.getPrecioBase() : 0;
            }
            if (lote.getEstadoActual() == EstadoLote.SOLICITUD_RECHAZADA) {
                rechazados++;
            }
        }

        return new MetricasVendedor(publicados, vendidos, rechazados, totalGenerado);
    }
}