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

        // 4. Validar importe según reglas del enunciado
        Optional<Pujo> mejorPujaActual = pujoRepository.findMejorPujaByItem(item.getIdentificador());
        BigDecimal precioBase = item.getPrecioBase();

        // Determinar si la categoría de la subasta exime del límite máximo
        String catSubasta = subasta.getCategoria() != null ? subasta.getCategoria().toLowerCase() : "";
        boolean sinLimiteMaximo = catSubasta.equals("oro") || catSubasta.equals("platino");

        // Incrementos reglamentarios: 1% y 20% del precio base
        BigDecimal unPorciento     = precioBase.multiply(new BigDecimal("0.01"));
        BigDecimal veintePorciento = precioBase.multiply(new BigDecimal("0.20"));

        if (mejorPujaActual.isPresent()) {
            BigDecimal mejorOferta = mejorPujaActual.get().getImporte();
            BigDecimal minimoRequerido = mejorOferta.add(unPorciento);
            BigDecimal maximoPermitido = mejorOferta.add(veintePorciento);

            // Validar mínimo (aplica a todas las categorías)
            if (request.getImporte().compareTo(minimoRequerido) < 0) {
                throw new PujaInvalidaException(String.format(
                        "La puja mínima es $%.2f (mejor oferta $%.2f + 1%% del precio base $%.2f)",
                        minimoRequerido, mejorOferta, precioBase));
            }

            // Validar máximo (solo para categorías comun, especial, plata)
            if (!sinLimiteMaximo && request.getImporte().compareTo(maximoPermitido) > 0) {
                throw new PujaInvalidaException(String.format(
                        "La puja máxima es $%.2f (mejor oferta $%.2f + 20%% del precio base $%.2f). " +
                        "Las subastas Oro y Platino no tienen límite superior.",
                        maximoPermitido, mejorOferta, precioBase));
            }
        } else {
            // Primera puja del item: debe ser al menos el precio base
            if (request.getImporte().compareTo(precioBase) < 0) {
                throw new PujaInvalidaException(String.format(
                        "La primera puja debe ser al menos el precio base: $%.2f", precioBase));
            }
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
                .mensaje(String.format("Puja aceptada — Oferta actual: $%.2f", request.getImporte()))
                // Límites para la SIGUIENTE puja (útil para validación client-side en Android)
                .siguientePujaMinima(request.getImporte().add(unPorciento))
                .siguientePujaMaxima(sinLimiteMaximo ? null : request.getImporte().add(veintePorciento))
                .sinLimiteMaximo(sinLimiteMaximo)
                .build();

    }

    /**
     * Cierra la puja de un item: marca al ganador, genera el registro de subasta.
     * Si nadie pujó, la empresa compra el item al precio base (enunciado pág. 5).
     * Llamado por el subastador cuando termina de vender un item.
     */
    @Transactional
    public PujaResponse cerrarItem(Integer subastaId, Integer itemId, String emailSubastador) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item no encontrado"));

        if ("si".equalsIgnoreCase(item.getSubastado())) {
            throw new PujaInvalidaException("Este item ya fue cerrado anteriormente");
        }

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // Marcar item como subastado en ambos casos
        item.setSubastado("si");
        itemCatalogoRepository.save(item);

        // ¿Hubo alguna puja ganadora?
        return pujoRepository.findGanadorByItem(itemId)
                .map(ganador -> cerrarConGanador(subasta, item, ganador))
                .orElseGet(() -> cerrarSinPujas(subasta, item));
    }

    /** Caso A: alguien pujó — el mayor postor gana. */
    private PujaResponse cerrarConGanador(Subasta subasta, ItemCatalogo item, Pujo ganador) {
        Cliente comprador = ganador.getAsistente().getCliente();

        // Calcular costo de envío (fijo o basado en peso/volumen, usamos un dummy fijo según requerimiento)
        BigDecimal costoEnvio = new BigDecimal("5000.00");

        RegistroSubasta registro = RegistroSubasta.builder()
                .subasta(subasta)
                .duenio(item.getProducto().getDuenio())
                .producto(item.getProducto())
                .cliente(comprador)
                .importe(ganador.getImporte())
                .comision(item.getComision())
                .costoEnvio(costoEnvio)
                .compraEmpresa(false)
                .build();
        registroSubastaRepository.save(registro);

        BigDecimal totalPagar = ganador.getImporte().add(item.getComision()).add(costoEnvio);
        String detalleFactura = String.format(
                "Factura de Compra - Subasta %d\n" +
                "Item: '%s'\n" +
                "Importe Pujado: $%.2f\n" +
                "Comisión Empresa: $%.2f\n" +
                "Costo de Envío a Domicilio: $%.2f\n" +
                "TOTAL A PAGAR: $%.2f",
                subasta.getIdentificador(),
                item.getProducto().getDescripcionCompleta(),
                ganador.getImporte(),
                item.getComision(),
                costoEnvio,
                totalPagar
        );

        // Notificación in-app al ganador
        notificacionService.crear(
                comprador,
                TipoNotificacion.PUJA_GANADA,
                "¡Ganaste la subasta!",
                detalleFactura,
                (long) item.getIdentificador(), "ITEM"
        );

        // Log para email simulando envío de factura detallada
        usuarioAuthRepository
                .findByPersonaIdentificador(comprador.getIdentificador())
                .ifPresent(auth ->
                    log.info(">> ENVIANDO EMAIL POST-VENTA A: {} <<\n{}", auth.getEmail(), detalleFactura));

        return PujaResponse.builder()
                .pujoId(ganador.getIdentificador())
                .itemId(item.getIdentificador())
                .nombrePostor(comprador.getPersona().getNombre())
                .numeroPostor(ganador.getAsistente().getNumeroPostor())
                .importe(ganador.getImporte())
                .precioBase(item.getPrecioBase())
                .fechaHora(ganador.getFechaHora())
                .esGanadora(true)
                .mensaje(String.format("ITEM VENDIDO — Ganador: %s por $%.2f",
                        comprador.getPersona().getNombre(), ganador.getImporte()))
                .build();
    }

    /**
     * Caso B: nadie pujó — la empresa compra al precio base.
     * Se usa el cliente del sistema (documento="SISTEMA_EMPRESA") creado por el seed.
     * Si por algún motivo no existe, se lanza excepción descriptiva.
     */
    private PujaResponse cerrarSinPujas(Subasta subasta, ItemCatalogo item) {
        log.info("Item {} sin pujas — empresa compra al precio base ${}",
                item.getIdentificador(), item.getPrecioBase());

        Cliente empresa = clienteRepository.findByPersonaDocumento("SISTEMA_EMPRESA")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente sistema 'EMPRESA' no encontrado. Ejecute el script de seed (02_seed_data.sql)."));

        // La comisión en compra por la empresa es $0 (no hay comisión interna)
        RegistroSubasta registro = RegistroSubasta.builder()
                .subasta(subasta)
                .duenio(item.getProducto().getDuenio())
                .producto(item.getProducto())
                .cliente(empresa)
                .importe(item.getPrecioBase())
                .comision(BigDecimal.ZERO)
                .compraEmpresa(true)
                .build();
        registroSubastaRepository.save(registro);

        log.info("Compra empresa registrada — item={} | precio base=${}",
                item.getIdentificador(), item.getPrecioBase());

        return PujaResponse.builder()
                .itemId(item.getIdentificador())
                .importe(item.getPrecioBase())
                .precioBase(item.getPrecioBase())
                .esGanadora(false)
                .mensaje(String.format(
                        "Sin postores — la empresa adquiere el item al precio base: $%.2f",
                        item.getPrecioBase()))
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
