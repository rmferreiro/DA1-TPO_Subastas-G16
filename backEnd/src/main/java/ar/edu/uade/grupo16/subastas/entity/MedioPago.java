package ar.edu.uade.grupo16.subastas.entity;

import ar.edu.uade.grupo16.subastas.enums.Moneda;
import ar.edu.uade.grupo16.subastas.enums.TipoMedioPago;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad NUEVA — Medios de pago del cliente.
 * No existe en la estructura Legacy.
 * Soporta: cuentas bancarias, tarjetas de crédito, cheques certificados.
 */
@Entity
@Table(name = "medios_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoMedioPago tipo;

    // --- Datos de Cuenta Bancaria ---
    @Column(length = 150)
    private String banco;

    @Column(name = "numero_cuenta", length = 50)
    private String numeroCuenta;

    @Column(name = "cbu_swift", length = 50)
    private String cbuSwift;

    @Column(name = "es_internacional")
    private Boolean esInternacional;

    // --- Datos de Tarjeta de Crédito ---
    @Column(name = "numero_tarjeta_hash", length = 255)
    private String numeroTarjetaHash; // Almacenado con hash por seguridad

    @Column(length = 150)
    private String titular;

    @Column(length = 7)
    private String vencimiento; // MM/YYYY

    @Column(name = "es_tarjeta_internacional")
    private Boolean esTarjetaInternacional;

    // --- Datos de Cheque Certificado ---
    @Column(name = "numero_cheque", length = 50)
    private String numeroCheque;

    @Column(name = "banco_emisor", length = 150)
    private String bancoEmisor;

    @Column(name = "monto_certificado", precision = 18, scale = 2)
    private BigDecimal montoCertificado;

    // --- Campos comunes ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moneda moneda;

    @Builder.Default
    @Column(nullable = false)
    private Boolean verificado = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Monto actualmente reservado/comprometido por pujas activas
    @Builder.Default
    @Column(name = "monto_reservado", precision = 18, scale = 2)
    private BigDecimal montoReservado = BigDecimal.ZERO;
}
