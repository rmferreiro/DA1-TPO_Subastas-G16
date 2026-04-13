package ar.edu.uade.grupo16.subastas.service.strategy;

import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Strategy para Cheque Certificado.
 * Tiene un monto FIJO certificado. Solo puede usarse en ARS.
 * La reserva de fondos descuenta del monto certificado disponible.
 * Si el monto disponible es menor que la nueva puja, no puede licitar.
 */
@Component
public class ChequeCertificadoStrategy implements MedioPagoStrategy {

    @Override
    public boolean esValido(MedioPago medioPago) {
        return medioPago.getNumeroCheque() != null && !medioPago.getNumeroCheque().isBlank()
                && medioPago.getBancoEmisor() != null && !medioPago.getBancoEmisor().isBlank()
                && medioPago.getMontoCertificado() != null
                && medioPago.getMontoCertificado().compareTo(BigDecimal.ZERO) > 0
                && Boolean.TRUE.equals(medioPago.getVerificado());
    }

    @Override
    public boolean puedeOperarEnMoneda(MedioPago medioPago, Moneda moneda) {
        // Cheques certificados solo operan en ARS
        return moneda == Moneda.ARS;
    }

    @Override
    public BigDecimal getMontoDisponible(MedioPago medioPago) {
        BigDecimal certificado = medioPago.getMontoCertificado() != null
                ? medioPago.getMontoCertificado() : BigDecimal.ZERO;
        BigDecimal reservado = medioPago.getMontoReservado() != null
                ? medioPago.getMontoReservado() : BigDecimal.ZERO;
        BigDecimal disponible = certificado.subtract(reservado);
        return disponible.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : disponible;
    }

    @Override
    public boolean reservarFondos(MedioPago medioPago, BigDecimal monto) {
        if (getMontoDisponible(medioPago).compareTo(monto) < 0) {
            return false; // Fondos insuficientes en el cheque
        }
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
