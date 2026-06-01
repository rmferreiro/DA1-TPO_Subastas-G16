package ar.edu.uade.grupo16.subastas.service.strategy;

import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Strategy para Tarjeta de Crédito.
 * Para subastas en USD solo aplica si es tarjeta internacional.
 */
@Component
public class TarjetaCreditoStrategy implements MedioPagoStrategy {

    @Override
    public boolean esValido(MedioPago medioPago) {
        return medioPago.getNumeroTarjetaHash() != null
                && medioPago.getTitular() != null && !medioPago.getTitular().isBlank()
                && medioPago.getVencimiento() != null && !medioPago.getVencimiento().isBlank()
                && Boolean.TRUE.equals(medioPago.getVerificado());
    }

    @Override
    public boolean puedeOperarEnMoneda(MedioPago medioPago, Moneda moneda) {
        if (moneda == Moneda.USD) {
            // Para subasta en dólares se requiere tarjeta internacional
            return Boolean.TRUE.equals(medioPago.getEsTarjetaInternacional());
        }
        return true;
    }

    @Override
    public BigDecimal getMontoDisponible(MedioPago medioPago) {
        BigDecimal reservado = medioPago.getMontoReservado() != null
                ? medioPago.getMontoReservado() : BigDecimal.ZERO;
        return BigDecimal.valueOf(Long.MAX_VALUE).subtract(reservado);
    }

    @Override
    public boolean reservarFondos(MedioPago medioPago, BigDecimal monto) {
        BigDecimal actual = medioPago.getMontoReservado() != null
                ? medioPago.getMontoReservado() : BigDecimal.ZERO;
        medioPago.setMontoReservado(actual.add(monto));
        return true;
    }

    @Override
    public void liberarFondos(MedioPago medioPago, BigDecimal monto) {
        BigDecimal actual = medioPago.getMontoReservado() != null
                ? medioPago.getMontoReservado() : BigDecimal.ZERO;
        BigDecimal nuevo = actual.subtract(monto);
        medioPago.setMontoReservado(nuevo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : nuevo);
    }
}
