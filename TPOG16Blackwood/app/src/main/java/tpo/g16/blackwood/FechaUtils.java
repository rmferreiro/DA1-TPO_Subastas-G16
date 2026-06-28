package tpo.g16.blackwood;

import java.time.LocalDate;

/**
 * Formateo de fechas en español, en un solo lugar para que toda la app
 * muestre el mismo formato ("27 junio 2026") y se pueda comparar fechas
 * de verdad (LocalDate) en vez de comparar texto suelto.
 */
public class FechaUtils {

    private static final String[] NOMBRES_MES = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };

    private FechaUtils() {
        // Solo métodos estáticos, no se instancia
    }

    /** LocalDate.of(2026, 6, 27) -> "27 junio 2026" */
    public static String formatear(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.getDayOfMonth() + " " + NOMBRES_MES[fecha.getMonthValue() - 1] + " " + fecha.getYear();
    }
}
