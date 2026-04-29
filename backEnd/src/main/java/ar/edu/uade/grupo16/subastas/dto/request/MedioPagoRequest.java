package ar.edu.uade.grupo16.subastas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioPagoRequest {

    @NotBlank(message = "El tipo de medio de pago es obligatorio")
    private String tipo; // CUENTA_BANCARIA, TARJETA_CREDITO, CHEQUE_CERTIFICADO

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda; // ARS, USD

    // --- Datos de Cuenta Bancaria ---
    private String banco;
    private String numeroCuenta;
    private String cbuSwift;
    private Boolean esInternacional;

    // --- Datos de Tarjeta de Crédito ---
    private String numeroTarjeta;
    private String titular;
    private String vencimiento;
    private Boolean esTarjetaInternacional;

    // --- Datos de Cheque Certificado ---
    private String numeroCheque;
    private String bancoEmisor;
    private BigDecimal montoCertificado;
}
