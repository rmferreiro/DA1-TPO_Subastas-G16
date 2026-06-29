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
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<MedioPago> listarNoVerificados() {
        return medioPagoRepository.findByVerificadoFalseAndActivoTrue();
    }

    /**
     * Lista medios de pago pendientes de verificación para el panel admin.
     * El mapeo a Map ocurre dentro de la transacción para poder acceder a cliente.persona
     * con open-in-view=false.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarNoVerificadosParaAdmin() {
        return medioPagoRepository.findByVerificadoFalseAndActivoTrue()
                .stream()
                .map(this::toAdminResponseMap)
                .collect(Collectors.toList());
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
     * Actualiza los campos de detalle de un medio de pago existente.
     * No permite cambiar tipo, moneda ni cliente.
     */
    @Transactional
    public MedioPago actualizar(Long medioPagoId, Integer clienteId, MedioPagoRequest request) {
        MedioPago mp = medioPagoRepository.findById(medioPagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medio de pago no encontrado"));
        if (!mp.getCliente().getIdentificador().equals(clienteId)) {
            throw new RegistroInvalidoException("No podés editar un medio de pago que no te pertenece");
        }
        switch (mp.getTipo()) {
            case CUENTA_BANCARIA -> {
                if (request.getBanco() != null) mp.setBanco(request.getBanco());
                if (request.getNumeroCuenta() != null) mp.setNumeroCuenta(request.getNumeroCuenta());
                if (request.getCbuSwift() != null) mp.setCbuSwift(request.getCbuSwift());
                if (request.getEsInternacional() != null) mp.setEsInternacional(request.getEsInternacional());
            }
            case TARJETA_CREDITO -> {
                if (request.getNumeroTarjeta() != null)
                    mp.setNumeroTarjetaHash(passwordEncoder.encode(request.getNumeroTarjeta()));
                if (request.getTitular() != null) mp.setTitular(request.getTitular());
                if (request.getVencimiento() != null) mp.setVencimiento(request.getVencimiento());
                if (request.getEsTarjetaInternacional() != null) mp.setEsTarjetaInternacional(request.getEsTarjetaInternacional());
            }
            case CHEQUE_CERTIFICADO -> {
                if (request.getNumeroCheque() != null) mp.setNumeroCheque(request.getNumeroCheque());
                if (request.getBancoEmisor() != null) mp.setBancoEmisor(request.getBancoEmisor());
                if (request.getMontoCertificado() != null) mp.setMontoCertificado(request.getMontoCertificado());
            }
        }
        mp.setVerificado(false);
        return medioPagoRepository.save(mp);
    }

    /**
     * Resuelve el medio de pago a usar para una puja.
     * Usa las mismas reglas de compatibilidad que la app (strategy.puedeOperarEnMoneda).
     * Si se indica un ID preferido (el que el cliente ya validó al entrar), se usa ese.
     */
    @Transactional(readOnly = true)
    public MedioPago resolverMedioPagoParaPuja(Integer clienteId, Moneda moneda, Long medioPagoIdPreferido) {
        List<MedioPago> medios = medioPagoRepository
                .findByClienteIdentificadorAndVerificadoTrueAndActivoTrue(clienteId);

        List<MedioPago> compatibles = medios.stream()
                .filter(mp -> mp.getTipo() != null
                        && getStrategy(mp.getTipo()).puedeOperarEnMoneda(mp, moneda))
                .toList();

        if (compatibles.isEmpty()) {
            throw new MedioPagoRequeridoException(
                    "No tenés ningún medio de pago verificado compatible con esta subasta en " + moneda.name());
        }

        if (medioPagoIdPreferido != null) {
            for (MedioPago mp : compatibles) {
                if (medioPagoIdPreferido.equals(mp.getId())) {
                    return mp;
                }
            }
        }

        return compatibles.get(0);
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
        String monedaStr = mp.getMoneda() != null ? mp.getMoneda().name() : "ARS";
        Boolean verificado = mp.getVerificado() != null ? mp.getVerificado() : false;
        Boolean activo = mp.getActivo() != null ? mp.getActivo() : false;
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", mp.getId());
        result.put("tipo", mp.getTipo() != null ? mp.getTipo().name() : "DESCONOCIDO");
        result.put("moneda", monedaStr);
        result.put("verificado", verificado);
        result.put("activo", activo);
        result.put("esInternacional", Boolean.TRUE.equals(mp.getEsInternacional()));
        result.put("esTarjetaInternacional", Boolean.TRUE.equals(mp.getEsTarjetaInternacional()));
        if (mp.getTipo() != null) {
            result.put("montoDisponible", getStrategy(mp.getTipo()).getMontoDisponible(mp));
            result.put("detalle", buildDetalle(mp));
        } else {
            result.put("montoDisponible", java.math.BigDecimal.ZERO);
            result.put("detalle", "Tipo no definido");
        }
        return result;
    }

    public Map<String, Object> toAdminResponseMap(MedioPago mp) {
        Map<String, Object> map = new java.util.HashMap<>(toResponseMap(mp));
        String clienteNombre = (mp.getCliente() != null && mp.getCliente().getPersona() != null)
                ? mp.getCliente().getPersona().getNombre() : "Desconocido";
        map.put("clienteNombre", clienteNombre);
        return map;
    }

    private String buildDetalle(MedioPago mp) {
        if (mp.getTipo() == null) {
            return "Sin tipo definido";
        }
        return switch (mp.getTipo()) {
            case CUENTA_BANCARIA -> "Banco: " + safeStr(mp.getBanco()) +
                    " | Cuenta: ***" + truncar(mp.getNumeroCuenta(), 4) +
                    (Boolean.TRUE.equals(mp.getEsInternacional()) ? " (Internacional)" : "");
            case TARJETA_CREDITO -> "Titular: " + safeStr(mp.getTitular()) +
                    " | Vence: " + safeStr(mp.getVencimiento()) +
                    (Boolean.TRUE.equals(mp.getEsTarjetaInternacional()) ? " (Internacional)" : "");
            case CHEQUE_CERTIFICADO -> "Banco emisor: " + safeStr(mp.getBancoEmisor()) +
                    " | Monto certificado: $" + (mp.getMontoCertificado() != null ? mp.getMontoCertificado() : "0");
        };
    }

    private String safeStr(String valor) {
        return valor != null && !valor.isBlank() ? valor : "-";
    }

    private String truncar(String valor, int ultimos) {
        if (valor == null || valor.isBlank()) return "****";
        if (valor.length() <= ultimos) return valor;
        return valor.substring(valor.length() - ultimos);
    }
}
