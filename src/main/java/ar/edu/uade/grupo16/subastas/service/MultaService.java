package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.enums.TipoNotificacion;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MultaService {

    private static final Logger log = LoggerFactory.getLogger(MultaService.class);
    private static final BigDecimal PORCENTAJE_MULTA = new BigDecimal("0.10"); // 10%
    private static final int HORAS_LIMITE = 72;

    private final MultaRepository multaRepository;
    private final RegistroSubastaRepository registroSubastaRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final ClienteRepository clienteRepository;
    private final NotificacionService notificacionService;
    private final MailService mailService;

    public MultaService(MultaRepository multaRepository,
                        RegistroSubastaRepository registroSubastaRepository,
                        UsuarioAuthRepository usuarioAuthRepository,
                        ClienteRepository clienteRepository,
                        NotificacionService notificacionService,
                        MailService mailService) {
        this.multaRepository = multaRepository;
        this.registroSubastaRepository = registroSubastaRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.clienteRepository = clienteRepository;
        this.notificacionService = notificacionService;
        this.mailService = mailService;
    }

    /**
     * Genera una multa para un cliente que ganó un item pero no pagó.
     * Se llama cuando el subastador/sistema detecta la falta de pago.
     * Multa = 10% del importe ofertado.
     */
    @Transactional
    public Multa generarMulta(Integer clienteId, Integer subastaId, Integer itemId, BigDecimal montoOfertado) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        Subasta subasta = new Subasta();
        subasta.setIdentificador(subastaId);

        ItemCatalogo item = new ItemCatalogo();
        item.setIdentificador(itemId);

        BigDecimal montoMulta = montoOfertado
                .multiply(PORCENTAJE_MULTA)
                .setScale(2, RoundingMode.HALF_UP);

        LocalDateTime ahora = LocalDateTime.now();

        Multa multa = Multa.builder()
                .cliente(cliente)
                .subasta(subasta)
                .item(item)
                .montoOfertado(montoOfertado)
                .montoMulta(montoMulta)
                .fechaLimite(ahora.plusHours(HORAS_LIMITE))
                .fechaCreacion(ahora)
                .build();

        multa = multaRepository.save(multa);

        // Notificar al cliente
        notificacionService.crear(
                cliente,
                TipoNotificacion.MULTA,
                "Tenés una multa pendiente",
                String.format("No se registró el pago por tu compra. " +
                        "Multa: $%.2f (10%% de $%.2f). " +
                        "Tenés %d horas para pagar antes de ser bloqueado.",
                        montoMulta, montoOfertado, HORAS_LIMITE),
                (long) itemId, "ITEM"
        );

        log.info("Multa generada — Cliente: {} | Item: {} | Monto: ${}",
                clienteId, itemId, montoMulta);

        return multa;
    }

    /**
     * Marca una multa como pagada.
     */
    @Transactional
    public Map<String, Object> pagarMulta(Long multaId, Integer clienteId) {
        Multa multa = multaRepository.findById(multaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Multa no encontrada"));

        if (!multa.getCliente().getIdentificador().equals(clienteId)) {
            throw new RecursoNoEncontradoException("La multa no pertenece a este cliente");
        }

        if (Boolean.TRUE.equals(multa.getPagada())) {
            return Map.of("mensaje", "La multa ya fue pagada anteriormente");
        }

        multa.setPagada(true);
        multaRepository.save(multa);

        log.info("Multa pagada — ID: {} | Cliente: {}", multaId, clienteId);

        return Map.of(
                "mensaje", "Multa pagada exitosamente. Ya podés participar en subastas.",
                "multaId", multaId,
                "montoPagado", multa.getMontoMulta()
        );
    }

    /**
     * Lista las multas pendientes de un cliente.
     */
    public List<Map<String, Object>> listarMultasPendientes(Integer clienteId) {
        return multaRepository.findByClienteIdentificadorAndPagadaFalse(clienteId)
                .stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "montoOfertado", m.getMontoOfertado(),
                        "montoMulta", m.getMontoMulta(),
                        "fechaLimite", m.getFechaLimite(),
                        "derivadoJusticia", m.getDerivadoJusticia(),
                        "horasRestantes", calcularHorasRestantes(m)
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarTodasLasMultas(Integer clienteId) {
        return multaRepository.findByClienteIdentificador(clienteId)
                .stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "montoOfertado", m.getMontoOfertado(),
                        "montoMulta", m.getMontoMulta(),
                        "pagada", m.getPagada(),
                        "fechaLimite", m.getFechaLimite(),
                        "derivadoJusticia", m.getDerivadoJusticia()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Job programado: ejecuta cada hora y verifica multas vencidas.
     * Si pasaron las 72hs sin pago → bloquea usuario + deriva a justicia.
     */
    @Scheduled(fixedDelay = 3600000) // cada 1 hora
    @Transactional
    public void verificarMultasVencidas() {
        List<Multa> vencidas = multaRepository
                .findByPagadaFalseAndDerivadoJusticiaFalseAndFechaLimiteBefore(LocalDateTime.now());

        for (Multa multa : vencidas) {
            multa.setDerivadoJusticia(true);
            multaRepository.save(multa);

            // Bloquear cliente en UsuarioAuth
            usuarioAuthRepository.findByPersonaIdentificador(multa.getCliente().getIdentificador())
                    .ifPresent(auth -> {
                        auth.setEstado(ar.edu.uade.grupo16.subastas.enums.EstadoUsuario.BLOQUEADO);
                        usuarioAuthRepository.save(auth);

                        // Notificar bloqueo
                        notificacionService.crear(
                                multa.getCliente(),
                                TipoNotificacion.MULTA,
                                "Cuenta bloqueada por multa impaga",
                                String.format("Tu multa de $%.2f no fue pagada en el plazo. " +
                                        "Tu cuenta fue bloqueada y el caso fue derivado a la justicia.",
                                        multa.getMontoMulta()),
                                multa.getId(), "MULTA"
                        );

                        log.warn("Cliente bloqueado por multa impaga — Cliente: {} | Multa: {}",
                                multa.getCliente().getIdentificador(), multa.getId());
                    });
        }

        if (!vencidas.isEmpty()) {
            log.info("Job multas: {} clientes bloqueados por multas vencidas", vencidas.size());
        }
    }

    private long calcularHorasRestantes(Multa multa) {
        LocalDateTime ahora = LocalDateTime.now();
        if (multa.getFechaLimite().isBefore(ahora)) return 0;
        return java.time.Duration.between(ahora, multa.getFechaLimite()).toHours();
    }
}
