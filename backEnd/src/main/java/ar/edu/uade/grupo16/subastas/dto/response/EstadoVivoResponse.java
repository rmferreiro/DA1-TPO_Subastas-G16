package ar.edu.uade.grupo16.subastas.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Estado en vivo de una subasta para la pantalla principal de la app Android.
 * Se obtiene vía GET /api/subastas/{id}/estado-vivo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoVivoResponse {

    // -- Datos de la subasta --
    private Integer subastaId;
    private String estadoSubasta;       // "abierta" | "cerrada"
    private String moneda;              // "ARS" | "USD"
    private String categoria;           // categoría de la subasta
    private int itemsRestantes;         // items aún no subastados
    private int itemsSubastados;        // items ya cerrados
    private Long limiteFinalizacionEpoch;

    // -- Item actualmente en subasta --
    /** null si no hay más items pendientes */
    private ItemActivoInfo itemActual;

    // -- Historial de pujas del item actual (últimas 10, más reciente primero) --
    private List<PujaInfo> ultimasPujas;

    // ── Inner DTOs ──────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemActivoInfo {
        private Integer itemId;
        private Integer productoId;
        /** Número de orden de presentación en el catálogo (1, 2, 3…). Usar para mostrar "Lote #N" en la UI. */
        private Integer orden;
        private String descripcion;
        private String descripcionCompleta;
        private BigDecimal precioBase;
        private BigDecimal mejorOferta;       // null si nadie pujó aún
        private String nombreMejorPostor;     // null si nadie pujó aún
        private int totalPujas;

        /** Próxima puja mínima válida (mejorOferta + 1% del precioBase) */
        private BigDecimal siguientePujaMinima;
        /** Próxima puja máxima válida. null si subasta es Oro/Platino */
        private BigDecimal siguientePujaMaxima;
        /** true si categoría es Oro o Platino → sin límite superior */
        private boolean sinLimiteMaximo;

        // Datos extra para obras de arte
        private String artista;
        private String historia;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PujaInfo {
        private Integer pujoId;
        private String nombrePostor;
        private Integer numeroPostor;
        private BigDecimal importe;
        private LocalDateTime fechaHora;
        private boolean esGanadora;
    }
}
