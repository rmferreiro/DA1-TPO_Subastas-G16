package ar.edu.uade.grupo16.subastas.service.strategy;

import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Strategy para Cuenta Bancaria.
 * Puede ser nacional o internacional. Sin límite de monto simulado.
 * Para subastas en USD solo aplica si es cuenta internacional.
 */
@Component
public class CuentaBancariaStrategy implements MedioPagoStrategy {

    @Override
    public boolean esValido(MedioPago medioPago) {
        return medioPago.getBanco() != null && !medioPago.getBanco().isBlank()
                && medioPago.getNumeroCuenta() != null && !medioPago.getNumeroCuenta().isBlank()
                && Boolean.TRUE.equals(medioPago.getVerificado());
    }

    @Override
    public boolean puedeOperarEnMoneda(MedioPago medioPago, Moneda moneda) {
        if (moneda == Moneda.USD) {
            // Para subasta en dólares se requiere cuenta internacional
            return Boolean.TRUE.equals(medioPago.getEsInternacional());
        }
        return true; // ARS: cualquier cuenta
    }

    @Override
    public BigDecimal getMontoDisponible(MedioPago medioPago) {
        // Sin límite conocido para cuentas bancarias
        // Retornamos MAX_VALUE menos lo reservado (reservado no puede ser negativo)
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
