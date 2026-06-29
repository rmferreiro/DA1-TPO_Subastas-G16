package tpo.g16.blackwood;

/**
 * Estados posibles de un lote propuesto por un usuario para subasta.
 * Flujo:
 *   PENDIENTE_INSPECCION
 *        |--> RECHAZADO (empresa rechaza tras inspeccionar el bien)
 *        |--> PROPUESTA_ENVIADA (empresa fija valor base + comisión)
 *                  |--> ACEPTADO_USUARIO --> INCLUIDO_SUBASTA
 *                  |--> RECHAZADO_USUARIO (usuario no acepta condiciones)
 */
public enum EstadoLote {
    PENDIENTE_INSPECCION("PENDIENTE DE INSPECCIÓN", 0xFFC6A75E),
    RECHAZADO("RECHAZADO", 0xFFE53935),
    PROPUESTA_ENVIADA("PROPUESTA ENVIADA", 0xFF2F6FAD),
    ACEPTADO_USUARIO("ACEPTADO POR USUARIO", 0xFF4CAF50),
    RECHAZADO_USUARIO("RECHAZADO POR USUARIO", 0xFFE53935),
    INCLUIDO_SUBASTA("INCLUIDO EN SUBASTA", 0xFF1C2A21);

    private final String etiqueta;
    private final int color;

    EstadoLote(String etiqueta, int color) {
        this.etiqueta = etiqueta;
        this.color = color;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getColor() {
        return color;
    }
}
