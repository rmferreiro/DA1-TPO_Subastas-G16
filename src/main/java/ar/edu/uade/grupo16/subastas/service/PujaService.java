package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.request.PujaRequest;
import ar.edu.uade.grupo16.subastas.dto.response.PujaResponse;
import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import ar.edu.uade.grupo16.subastas.enums.TipoNotificacion;
import ar.edu.uade.grupo16.subastas.exception.PujaInvalidaException;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.SubastaNoDisponibleException;
import ar.edu.uade.grupo16.subastas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PujaService {

    private static final Logger log = LoggerFactory.getLogger(PujaService.class);

    private final PujoRepository pujoRepository;
    private final AsistenteRepository asistenteRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final SubastaRepository subastaRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final ClienteRepository clienteRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final RegistroSubastaRepository registroSubastaRepository;
    private final MedioPagoService medioPagoService;
    private final NotificacionService notificacionService;

    public PujaService(PujoRepository pujoRepository,
                       AsistenteRepository asistenteRepository,
                       ItemCatalogoRepository itemCatalogoRepository,
                       SubastaRepository subastaRepository,
                       UsuarioAuthRepository usuarioAuthRepository,
                       ClienteRepository clienteRepository,
                       MedioPagoRepository medioPagoRepository,
                       RegistroSubastaRepository registroSubastaRepository,
                       MedioPagoService medioPagoService,
                       NotificacionService notificacionService) {
        this.pujoRepository = pujoRepository;
        this.asistenteRepository = asistenteRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.subastaRepository = subastaRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.clienteRepository = clienteRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.registroSubastaRepository = registroSubastaRepository;
        this.medioPagoService = medioPagoService;
        this.notificacionService = notificacionService;
    }

    /**
     * Procesa una puja. Llamado ÚNICAMENTE desde SubastaSalaManager (worker thread).
     * No necesita sincronización adicional porque ya hay 1 solo thread por subasta.
     */
    @Transactional
    public PujaResponse procesarPuja(Integer subastaId, PujaRequest request, String emailPostor) {
        // 1. Cargar la subasta
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        if (!"abierta".equalsIgnoreCase(subasta.getEstado())) {
            throw new SubastaNoDisponibleException("La subasta está cerrada");
        }

        // 2. Cargar el item
        ItemCatalogo item = itemCatalogoRepository.findById(request.getItemId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Item no encontrado"));

        if ("si".equalsIgnoreCase(item.getSubastado())) {
            throw new PujaInvalidaException("Este item ya fue subastado");
        }

        // Verificar que el item pertenece a esta subasta
        if (!item.getCatalogo().getSubasta().getIdentificador().equals(subastaId)) {
            throw new PujaInvalidaException("El item no pertenece a esta subasta");
        }

        // 3. Obtener el asistente
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(emailPostor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        Asistente asistente = asistenteRepository
                .findByClienteIdentificadorAndSubastaIdentificador(cliente.getIdentificador(), subastaId)
                .orElseThrow(() -> new PujaInvalidaException("No estás registrado como asistente de esta subasta"));

        // 4. Validar importe mínimo
        Optional<Pujo> mejorPujaActual = pujoRepository.findMejorPujaByItem(item.getIdentificador());
        BigDecimal precioBase = item.getPrecioBase();
        BigDecimal minimoRequerido = mejorPujaActual
                .map(p -> p.getImporte().add(BigDecimal.ONE))
                .orElse(precioBase);

        if (request.getImporte().compareTo(minimoRequerido) < 0) {
            throw new PujaInvalidaException(
                    "La puja debe ser mayor a " + minimoRequerido +
                    ". Precio base: " + precioBase);
        }

        // 5. Validar medio de pago y reservar fondos
        Moneda monedaSubasta = subasta.getMoneda() != null ? subasta.getMoneda() : Moneda.ARS;
        MedioPago medioPago = medioPagoService.obtenerMedioPagoValidoParaSubasta(
                request.getMedioPagoId(), cliente.getIdentificador(), monedaSubasta);

        // Calcular el monto que ya tenía reservado para este item (si pujó antes)
        BigDecimal montoAnteriorReservado = calcularReservaAnterior(asistente, item);

        // Reservar los fondos (libera los anteriores del mismo postor y reserva los nuevos)
        medioPagoService.reservarParaPuja(medioPago, request.getImporte(), montoAnteriorReservado);

        // 6. Crear el nuevo pujo
        Pujo nuevoPujo = Pujo.builder()
                .asistente(asistente)
                .item(item)
                .importe(request.getImporte())
                .ganador("si") // provisionalmente ganador
                .fechaHora(LocalDateTime.now())
                .build();
        pujoRepository.save(nuevoPujo);

        // 7. Marcar como NO ganador al anterior ganador
        BigDecimal importeAnterior = BigDecimal.ZERO;
        Cliente clienteAnteriorGanador = null;
        if (mejorPujaActual.isPresent()) {
            Pujo anteriorGanador = mejorPujaActual.get();
            importeAnterior = anteriorGanador.getImporte();
            clienteAnteriorGanador = anteriorGanador.getAsistente().getCliente();

            anteriorGanador.setGanador("no");
            pujoRepository.save(anteriorGanador);

            // Liberar fondos del anterior ganador (si no es el mismo postor)
            if (!anteriorGanador.getAsistente().getCliente().getIdentificador()
                    .equals(cliente.getIdentificador())) {
                var mpAnterior = medioPagoRepository
                        .findByClienteIdentificadorAndVerificadoTrueAndActivoTrue(
                                anteriorGanador.getAsistente().getCliente().getIdentificador())
                        .stream().findFirst();
                mpAnterior.ifPresent(mp ->
                        medioPagoService.liberarReserva(mp, anteriorGanador.getImporte()));
            }

            // Notificar al anterior ganador que fue superado
            if (clienteAnteriorGanador != null &&
                    !clienteAnteriorGanador.getIdentificador().equals(cliente.getIdentificador())) {
                notificacionService.crear(
                        clienteAnteriorGanador,
                        TipoNotificacion.PUJA_SUPERADA,
                        "Tu puja fue superada",
                        "Tu oferta de $" + importeAnterior + " por '" +
                                item.getProducto().getDescripcionCompleta() +
                                "' fue superada por $" + request.getImporte(),
                        (long) item.getIdentificador(), "ITEM"
                );
            }
        }

        log.info("Puja aceptada — Item: {} | Postor: {} | Importe: {} | Anterior: {}",
                item.getIdentificador(), emailPostor, request.getImporte(), importeAnterior);

        return PujaResponse.builder()
                .pujoId(nuevoPujo.getIdentificador())
                .itemId(item.getIdentificador())
                .nombrePostor(auth.getPersona().getNombre())
                .numeroPostor(asistente.getNumeroPostor())
                .importe(request.getImporte())
                .precioBase(precioBase)
                .mejorOfertaAnterior(importeAnterior.compareTo(BigDecimal.ZERO) > 0
                        ? importeAnterior : null)
                .fechaHora(nuevoPujo.getFechaHora())
                .esGanadora(true)
                .mensaje("Puja aceptada — Oferta actual: $" + request.getImporte())
                .build();
    }

    /**
     * Cierra la puja de un item: marca al ganador, genera el registro de subasta.
     * Llamado por el subastador cuando termina de vender un item.
     */
    @Transactional
    public PujaResponse cerrarItem(Integer subastaId, Integer itemId, String emailSubastador) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item no encontrado"));

        Pujo ganador = pujoRepository.findGanadorByItem(itemId)
                .orElseThrow(() -> new PujaInvalidaException("No hay pujas para este item"));

        // Marcar item como subastado
        item.setSubastado("si");
        itemCatalogoRepository.save(item);

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // Crear el registro de venta
        RegistroSubasta registro = RegistroSubasta.builder()
                .subasta(subasta)
                .duenio(item.getProducto().getDuenio())
                .producto(item.getProducto())
                .cliente(ganador.getAsistente().getCliente())
                .importe(ganador.getImporte())
                .comision(item.getComision())
                .build();
        registroSubastaRepository.save(registro);

        // Notificar al ganador
        notificacionService.crear(
                ganador.getAsistente().getCliente(),
                TipoNotificacion.PUJA_GANADA,
                "¡Ganaste la subasta!",
                "Ganaste '" + item.getProducto().getDescripcionCompleta() +
                        "' por $" + ganador.getImporte() +
                        ". Comisión: $" + item.getComision(),
                (long) itemId, "ITEM"
        );

        // Enviar email
        UsuarioAuth authGanador = usuarioAuthRepository
                .findByPersonaIdentificador(ganador.getAsistente().getCliente().getIdentificador())
                .orElse(null);
        if (authGanador != null) {
            // mailService se puede inyectar aquí si se desea
            log.info("EMAIL pendiente: enviar notificación de victoria a {}", authGanador.getEmail());
        }

        return PujaResponse.builder()
                .pujoId(ganador.getIdentificador())
                .itemId(itemId)
                .nombrePostor(ganador.getAsistente().getCliente().getPersona().getNombre())
                .numeroPostor(ganador.getAsistente().getNumeroPostor())
                .importe(ganador.getImporte())
                .fechaHora(ganador.getFechaHora())
                .esGanadora(true)
                .mensaje("ITEM VENDIDO — Ganador: " +
                        ganador.getAsistente().getCliente().getPersona().getNombre() +
                        " por $" + ganador.getImporte())
                .build();
    }

    private BigDecimal calcularReservaAnterior(Asistente asistente, ItemCatalogo item) {
        return pujoRepository.findByItemIdentificadorOrderByFechaHoraAsc(item.getIdentificador())
                .stream()
                .filter(p -> p.getAsistente().getIdentificador().equals(asistente.getIdentificador()))
                .map(Pujo::getImporte)
                .reduce(BigDecimal.ZERO, (max, val) -> val.compareTo(max) > 0 ? val : max);
    }
}
