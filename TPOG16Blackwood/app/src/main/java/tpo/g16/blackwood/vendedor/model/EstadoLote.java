package tpo.g16.blackwood.vendedor.model;

public enum EstadoLote {
    SOLICITUD_EN_PROCESO("Solicitud en proceso"),
    INICIO_TASACION("Inicio tasación"),
    EN_PROCESO("En proceso"),
    SOLICITUD_APROBADA("Solicitud aprobada"),
    SOLICITUD_RECHAZADA("Solicitud rechazada"),
    EN_SUBASTA("En subasta"),
    LOTE_COMPRADO("Lote comprado");

    private final String nombreVisual;

    EstadoLote(String nombreVisual) {
        this.nombreVisual = nombreVisual;
    }

    public String getNombreVisual() {
        return nombreVisual;
    }
}