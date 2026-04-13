package ar.edu.uade.grupo16.subastas.service.strategy;

import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import java.math.BigDecimal;

/**
 * Interfaz Strategy para los medios de pago.
 * Cada implementación maneja las reglas de un tipo específico de medio de pago.
 */
public interface MedioPagoStrategy {

    /**
     * Valida que el medio de pago esté correctamente configurado para operar.
     */
    boolean esValido(MedioPago medioPago);

    /**
     * Indica si este medio de pago puede operar en la moneda dada.
     * Clave para subastas en USD: solo cuentas internacionales o tarjetas internacionales.
     */
    boolean puedeOperarEnMoneda(MedioPago medioPago, Moneda moneda);

    /**
     * Retorna el monto disponible (libre de reservas) para este medio de pago.
     * Para cheques: monto certificado - monto reservado.
     * Para cuentas/tarjetas: BigDecimal.MAX_VALUE (sin límite simulado).
     */
    BigDecimal getMontoDisponible(MedioPago medioPago);

    /**
     * Reserva un monto para una puja activa.
     * El monto queda comprometido hasta que otro supere la puja o finalice la subasta.
     */
    boolean reservarFondos(MedioPago medioPago, BigDecimal monto);

    /**
     * Libera fondos previamente reservados (cuando la puja es superada).
     */
    void liberarFondos(MedioPago medioPago, BigDecimal monto);
}
