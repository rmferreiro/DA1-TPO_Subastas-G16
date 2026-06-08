package tpo.g16.blackwood.network.model;

import java.math.BigDecimal;

public class MedioPagoRequest {
    private String tipo; // CUENTA_BANCARIA, TARJETA_CREDITO, CHEQUE_CERTIFICADO
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

    // Builder pattern simplificado o Setters
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public void setBanco(String banco) { this.banco = banco; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public void setCbuSwift(String cbuSwift) { this.cbuSwift = cbuSwift; }
    public void setEsInternacional(Boolean esInternacional) { this.esInternacional = esInternacional; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }
    public void setTitular(String titular) { this.titular = titular; }
    public void setVencimiento(String vencimiento) { this.vencimiento = vencimiento; }
    public void setEsTarjetaInternacional(Boolean esTarjetaInternacional) { this.esTarjetaInternacional = esTarjetaInternacional; }
    public void setNumeroCheque(String numeroCheque) { this.numeroCheque = numeroCheque; }
    public void setBancoEmisor(String bancoEmisor) { this.bancoEmisor = bancoEmisor; }
    public void setMontoCertificado(BigDecimal montoCertificado) { this.montoCertificado = montoCertificado; }
}
