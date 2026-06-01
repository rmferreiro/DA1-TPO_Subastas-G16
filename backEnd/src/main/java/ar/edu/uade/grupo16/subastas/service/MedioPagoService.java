package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.request.MedioPagoRequest;
import ar.edu.uade.grupo16.subastas.entity.Cliente;
import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import ar.edu.uade.grupo16.subastas.enums.TipoMedioPago;
import ar.edu.uade.grupo16.subastas.exception.FondosInsuficientesException;
import ar.edu.uade.grupo16.subastas.exception.MedioPagoRequeridoException;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException;
import ar.edu.uade.grupo16.subastas.repository.ClienteRepository;
import ar.edu.uade.grupo16.subastas.repository.MedioPagoRepository;
import ar.edu.uade.grupo16.subastas.service.strategy.ChequeCertificadoStrategy;
import ar.edu.uade.grupo16.subastas.service.strategy.CuentaBancariaStrategy;
import ar.edu.uade.grupo16.subastas.service.strategy.MedioPagoStrategy;
import ar.edu.uade.grupo16.subastas.service.strategy.TarjetaCreditoStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MedioPagoService {

    private final MedioPagoRepository medioPagoRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaBancariaStrategy cuentaBancariaStrategy;
    private final TarjetaCreditoStrategy tarjetaCreditoStrategy;
    private final ChequeCertificadoStrategy chequeCertificadoStrategy;
    private final PasswordEncoder passwordEncoder;

    public MedioPagoService(MedioPagoRepository medioPagoRepository,
                            ClienteRepository clienteRepository,
                            CuentaBancariaStrategy cuentaBancariaStrategy,
                            TarjetaCreditoStrategy tarjetaCreditoStrategy,
                            ChequeCertificadoStrategy chequeCertificadoStrategy,
                            PasswordEncoder passwordEncoder) {
        this.medioPagoRepository = medioPagoRepository;
        this.clienteRepository = clienteRepository;
        this.cuentaBancariaStrategy = cuentaBancariaStrategy;
        this.tarjetaCreditoStrategy = tarjetaCreditoStrategy;
        this.chequeCertificadoStrategy = chequeCertificadoStrategy;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene la strategy correspondiente al tipo de medio de pago.
     */
    public MedioPagoStrategy getStrategy(TipoMedioPago tipo) {
        return switch (tipo) {
            case CUENTA_BANCARIA -> cuentaBancariaStrategy;
            case TARJETA_CREDITO -> tarjetaCreditoStrategy;
            case CHEQUE_CERTIFICADO -> chequeCertificadoStrategy;
        };
    }

    @Transactional
    public MedioPago registrar(Integer clienteId, MedioPagoRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        TipoMedioPago tipo;
        try {
            tipo = TipoMedioPago.valueOf(request.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegistroInvalidoException("Tipo de medio de pago inválido: " + request.getTipo());
        }

        Moneda moneda;
        try {
            moneda = Moneda.valueOf(request.getMoneda().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegistroInvalidoException("Moneda inválida: " + request.getMoneda());
        }

        MedioPago medioPago = MedioPago.builder()
                .cliente(cliente)
                .tipo(tipo)
                .moneda(moneda)
                .fechaRegistro(LocalDateTime.now())
                .build();

        // Poblar campos según el tipo
        switch (tipo) {
            case CUENTA_BANCARIA -> {
                if (request.getBanco() == null || request.getNumeroCuenta() == null) {
                    throw new RegistroInvalidoException("Cuenta bancaria requiere banco y número de cuenta");
                }
                medioPago.setBanco(request.getBanco());
                medioPago.setNumeroCuenta(request.getNumeroCuenta());
                medioPago.setCbuSwift(request.getCbuSwift());
                medioPago.setEsInternacional(
                        request.getEsInternacional() != null && request.getEsInternacional());
            }
            case TARJETA_CREDITO -> {
                if (request.getNumeroTarjeta() == null || request.getTitular() == null) {
                    throw new RegistroInvalidoException("Tarjeta requiere número, titular y vencimiento");
                }
                // Hasheamos el número de tarjeta por seguridad
                medioPago.setNumeroTarjetaHash(passwordEncoder.encode(request.getNumeroTarjeta()));
                medioPago.setTitular(request.getTitular());
                medioPago.setVencimiento(request.getVencimiento());
                medioPago.setEsTarjetaInternacional(
                        request.getEsTarjetaInternacional() != null && request.getEsTarjetaInternacional());
            }
            case CHEQUE_CERTIFICADO -> {
                if (request.getNumeroCheque() == null || request.getMontoCertificado() == null) {
                    throw new RegistroInvalidoException("Cheque requiere número, banco emisor y monto certificado");
                }
                if (moneda == Moneda.USD) {
                    throw new RegistroInvalidoException("Los cheques certificados solo operan en ARS");
                }
                medioPago.setNumeroCheque(request.getNumeroCheque());
                medioPago.setBancoEmisor(request.getBancoEmisor());
                medioPago.setMontoCertificado(request.getMontoCertificado());
            }
        }

        return medioPagoRepository.save(medioPago);
    }

    public List<MedioPago> listarPorCliente(Integer clienteId) {
        return medioPagoRepository.findByClienteIdentificadorAndActivoTrue(clienteId);
    }

    public List<MedioPago> listarVerificadosPorClienteYMoneda(Integer clienteId, Moneda moneda) {
        return medioPagoRepository.findByClienteIdentificadorAndMonedaAndVerificadoTrueAndActivoTrue(clienteId, moneda);
    }

    @Transactional
    public void verificar(Long medioPagoId) {
        MedioPago mp = medioPagoRepository.findById(medioPagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medio de pago no encontrado"));
        mp.setVerificado(true);
        medioPagoRepository.save(mp);
    }

    @Transactional
    public void desactivar(Long medioPagoId, Integer clienteId) {
        MedioPago mp = medioPagoRepository.findById(medioPagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medio de pago no encontrado"));
        if (!mp.getCliente().getIdentificador().equals(clienteId)) {
            throw new RegistroInvalidoException("No podés eliminar un medio de pago que no te pertenece");
        }
        mp.setActivo(false);
        medioPagoRepository.save(mp);
    }

    /**
     * Valida que el cliente tenga al menos un medio de pago verificado
     * compatible con la moneda de la subasta.
     */
    public MedioPago obtenerMedioPagoValidoParaSubasta(Long medioPagoId, Integer clienteId, Moneda moneda) {
        MedioPago mp = medioPagoRepository.findById(medioPagoId)
                .orElseThrow(() -> new MedioPagoRequeridoException("Medio de pago no encontrado"));

        if (!mp.getCliente().getIdentificador().equals(clienteId)) {
            throw new MedioPagoRequeridoException("El medio de pago no pertenece al cliente");
        }
        if (!Boolean.TRUE.equals(mp.getVerificado())) {
            throw new MedioPagoRequeridoException("El medio de pago no está verificado");
        }
        if (!Boolean.TRUE.equals(mp.getActivo())) {
            throw new MedioPagoRequeridoException("El medio de pago no está activo");
        }

        MedioPagoStrategy strategy = getStrategy(mp.getTipo());

        if (!strategy.puedeOperarEnMoneda(mp, moneda)) {
            throw new MedioPagoRequeridoException(
                    "Este medio de pago no puede operar en " + moneda.name() +
                    ". Registrá un medio de pago internacional.");
        }

        return mp;
    }

    /**
     * Reserva fondos en el medio de pago para una nueva puja.
     * Libera fondos de la puja anterior si corresponde.
     */
    @Transactional
    public void reservarParaPuja(MedioPago medioPago, BigDecimal montoNuevo, BigDecimal montoAnterior) {
        MedioPagoStrategy strategy = getStrategy(medioPago.getTipo());

        // Primero liberar monto anterior si el mismo cliente tenía una puja previa
        if (montoAnterior != null && montoAnterior.compareTo(BigDecimal.ZERO) > 0) {
            strategy.liberarFondos(medioPago, montoAnterior);
        }

        // Reservar nuevo monto
        boolean reservado = strategy.reservarFondos(medioPago, montoNuevo);
        if (!reservado) {
            throw new FondosInsuficientesException(
                    "Fondos insuficientes en el medio de pago para cubrir la puja de " + montoNuevo);
        }

        medioPagoRepository.save(medioPago);
    }

    @Transactional
    public void liberarReserva(MedioPago medioPago, BigDecimal monto) {
        MedioPagoStrategy strategy = getStrategy(medioPago.getTipo());
        strategy.liberarFondos(medioPago, monto);
        medioPagoRepository.save(medioPago);
    }

    /**
     * Construye un Map con los datos seguros del medio de pago (sin info sensible).
     */
    public Map<String, Object> toResponseMap(MedioPago mp) {
        return Map.of(
                "id", mp.getId(),
                "tipo", mp.getTipo().name(),
                "moneda", mp.getMoneda().name(),
                "verificado", mp.getVerificado(),
                "activo", mp.getActivo(),
                "montoDisponible", getStrategy(mp.getTipo()).getMontoDisponible(mp),
                "detalle", buildDetalle(mp)
        );
    }

    private String buildDetalle(MedioPago mp) {
        return switch (mp.getTipo()) {
            case CUENTA_BANCARIA -> "Banco: " + mp.getBanco() +
                    " | Cuenta: ***" + truncar(mp.getNumeroCuenta(), 4) +
                    (Boolean.TRUE.equals(mp.getEsInternacional()) ? " (Internacional)" : "");
            case TARJETA_CREDITO -> "Titular: " + mp.getTitular() +
                    " | Vence: " + mp.getVencimiento() +
                    (Boolean.TRUE.equals(mp.getEsTarjetaInternacional()) ? " (Internacional)" : "");
            case CHEQUE_CERTIFICADO -> "Banco emisor: " + mp.getBancoEmisor() +
                    " | Monto certificado: $" + mp.getMontoCertificado();
        };
    }

    private String truncar(String valor, int ultimos) {
        if (valor == null || valor.length() <= ultimos) return valor;
        return valor.substring(valor.length() - ultimos);
    }
}
