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

        if (!"ACTIVA".equalsIgnoreCase(subasta.getEstado())) {
            throw new SubastaNoDisponibleException("La subasta está cerrada");
        }

        // Verificar que el timer del lote no haya expirado en el servidor
        if (subasta.getLimiteFinalizacionEpoch() != null) {
            long ahora = java.time.Instant.now().toEpochMilli();
            if (subasta.getLimiteFinalizacionEpoch() <= ahora) {
                throw new PujaInvalidaException("El tiempo para pujar por este lote ha expirado");
            }
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

        // Solo se puede pujar por el lote activo de la subasta
        if (subasta.getItemActualId() != null
                && !subasta.getItemActualId().equals(request.getItemId())) {
            throw new PujaInvalidaException("Este lote ya no está activo. Esperá el siguiente.");
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

        // Enunciado p.3: los límites de mínimo y máximo NO aplican a oro/platino.
        // Para el resto: mínimo = última oferta + 1% base; máximo = última oferta + 20% base.
        String catSubasta = subasta.getCategoria() != null ? subasta.getCategoria().toLowerCase() : "";
        boolean sinLimiteMaximo = catSubasta.equals("oro") || catSubasta.equals("platino");

        // Incrementos reglamentarios para subastas estándar (comun, especial, plata)
        BigDecimal unPorciento     = precioBase.multiply(new BigDecimal("0.01"));
        BigDecimal veintePorciento = precioBase.multiply(new BigDecimal("0.20"));

        if (mejorPujaActual.isPresent()) {
            BigDecimal mejorOferta = mejorPujaActual.get().getImporte();

            if (sinLimiteMaximo) {
                // Oro/Platino: NINGÚN límite aplica. Solo debe superar la oferta actual.
                if (request.getImporte().compareTo(mejorOferta) <= 0) {
                    throw new PujaInvalidaException(String.format(
                            "La puja debe superar la oferta actual de $%.2f", mejorOferta));
                }
            } else {
                // Subastas estándar: mínimo = oferta + 1% base, máximo = oferta + 20% base
                BigDecimal minimoRequerido = mejorOferta.add(unPorciento);
                BigDecimal maximoPermitido = mejorOferta.add(veintePorciento);

                if (request.getImporte().compareTo(minimoRequerido) < 0) {
                    throw new PujaInvalidaException(String.format(
                            "La puja mínima es $%.2f (oferta actual $%.2f + 1%% del precio base $%.2f)",
                            minimoRequerido, mejorOferta, unPorciento));
                }
                if (request.getImporte().compareTo(maximoPermitido) > 0) {
                    throw new PujaInvalidaException(String.format(
                            "La puja máxima es $%.2f (oferta actual $%.2f + 20%% del precio base $%.2f)",
                            maximoPermitido, mejorOferta, veintePorciento));
                }
            }
        } else {
            // Primera puja del item: debe ser al menos el precio base (aplica a todas las categorías)
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

        // 8. Extender el límite de finalización de la subasta a 5 minutos en el futuro
        subasta.setLimiteFinalizacionEpoch(java.time.Instant.now().toEpochMilli() + 300000);
        subastaRepository.save(subasta);

        log.info("Puja aceptada — Item: {} | Postor: {} | Importe: {} | Anterior: {}",
                item.getIdentificador(), emailPostor, request.getImporte(), importeAnterior);

        // Calcular límites para la SIGUIENTE puja (validación client-side en Android)
        BigDecimal siguientePujaMinima;
        BigDecimal siguientePujaMaxima;
        if (sinLimiteMaximo) {
            // Oro/Platino: sin restricciones, indicar mínimo simbólico de +1 unidad
            siguientePujaMinima = request.getImporte().add(BigDecimal.ONE);
            siguientePujaMaxima = null;
        } else {
            siguientePujaMinima = request.getImporte().add(unPorciento);
            siguientePujaMaxima = request.getImporte().add(veintePorciento);
        }

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
                .siguientePujaMinima(siguientePujaMinima)
                .siguientePujaMaxima(siguientePujaMaxima)
                .sinLimiteMaximo(sinLimiteMaximo)
                .limiteFinalizacionEpoch(subasta.getLimiteFinalizacionEpoch())
                .build();

    }


    /**
     * Cierra la puja de un item: marca al ganador, genera el registro de subasta.
     * Si nadie pujó, la empresa compra el item al precio base (enunciado pág. 5).
     * Llamado por el subastador cuando termina de vender un item.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public PujaResponse cerrarItem(Integer subastaId, Integer itemId, String emailSubastador) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item no encontrado"));

        // Verificar si ya fue cerrado por otra petición (concurrencia)
        if ("si".equalsIgnoreCase(item.getSubastado())) {
            Optional<Pujo> ganadorOpt = pujoRepository.findGanadorByItem(itemId);
            if (ganadorOpt.isPresent()) {
                Pujo ganador = ganadorOpt.get();
                return PujaResponse.builder()
                    .pujoId(ganador.getIdentificador())
                    .itemId(itemId)
                    .nombrePostor(ganador.getAsistente().getCliente().getPersona().getNombre())
                    .numeroPostor(ganador.getAsistente().getNumeroPostor())
                    .importe(ganador.getImporte())
                    .fechaHora(ganador.getFechaHora())
                    .esGanadora(true)
                    .mensaje("ITEM YA ESTABA CERRADO")
                    .build();
            }
            return PujaResponse.builder().itemId(itemId).esGanadora(false).mensaje("ITEM YA ESTABA CERRADO SIN PUJAS").build();
        }

        // Marcar item como subastado SIEMPRE
        item.setSubastado("si");
        itemCatalogoRepository.save(item);

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // ¿Hubo alguna puja ganadora?
        return pujoRepository.findGanadorByItem(itemId)
                .map(ganador -> cerrarConGanador(subasta, item, ganador))
                .orElseGet(() -> cerrarSinPujas(subasta, item));
    }

    /** Caso A: alguien pujó — el mayor postor gana. */
    private PujaResponse cerrarConGanador(Subasta subasta, ItemCatalogo item, Pujo ganador) {
        Cliente comprador = ganador.getAsistente().getCliente();

        BigDecimal comision = item.getComision() != null ? item.getComision() : BigDecimal.ZERO;
        // Calcular costo de envío (fijo o basado en peso/volumen, usamos un dummy fijo según requerimiento)
        BigDecimal costoEnvio = new BigDecimal("5000.00");

        RegistroSubasta registro = RegistroSubasta.builder()
                .subasta(subasta)
                .duenio(item.getProducto().getDuenio())
                .producto(item.getProducto())
                .cliente(comprador)
                .importe(ganador.getImporte())
                .comision(comision)
                .costoEnvio(costoEnvio)
                .compraEmpresa(false)
                .pagado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
        registroSubastaRepository.save(registro);

        BigDecimal totalPagar = ganador.getImporte().add(comision).add(costoEnvio);
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
                comision,
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
     * Caso B: nadie pujó — Blackwood adquiere el item al precio base (enunciado pág. 5).
     * Se usa el cliente BLACKWOOD_SUBASTAS (identificador=9999) creado por el seed.
     */
    private PujaResponse cerrarSinPujas(Subasta subasta, ItemCatalogo item) {
        log.info("Item {} sin pujas — Blackwood adquiere al precio base ${}",
                item.getIdentificador(), item.getPrecioBase());

        Cliente blackwood = clienteRepository.findByPersonaDocumento("BLACKWOOD_SUBASTAS")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente Blackwood no encontrado. Ejecute el script de seed (02_seed_data.sql)."));

        BigDecimal costoEnvio = BigDecimal.ZERO;

        RegistroSubasta registro = RegistroSubasta.builder()
                .subasta(subasta)
                .duenio(item.getProducto().getDuenio())
                .producto(item.getProducto())
                .cliente(blackwood)
                .importe(item.getPrecioBase())
                .comision(BigDecimal.ZERO)
                .costoEnvio(costoEnvio)
                .compraEmpresa(true)
                .pagado(true)
                .fechaCreacion(LocalDateTime.now())
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

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> obtenerResultadoItem(Integer subastaId, Integer itemId, String email) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item no encontrado: " + itemId));
                
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
                
        Optional<Pujo> mejor = pujoRepository.findMejorPujaByItem(itemId);
        
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("itemId", itemId);
        resultado.put("subastaId", subastaId);
        resultado.put("productoDesc", item.getProducto().getDescripcionCompleta());
        resultado.put("subastaDesc", item.getCatalogo().getSubasta().getDescripcion());
        
        if (mejor.isPresent()) {
            Pujo ganador = mejor.get();
            boolean soyGanador = ganador.getAsistente().getCliente().getIdentificador().equals(auth.getPersona().getIdentificador());
            
            resultado.put("ganadorNombre", ganador.getAsistente().getCliente().getPersona().getNombre());
            resultado.put("soyGanador", soyGanador);
            resultado.put("importe", ganador.getImporte());
            resultado.put("pujoId", ganador.getIdentificador());
        } else {
            resultado.put("ganadorNombre", "Nadie");
            resultado.put("soyGanador", false);
            resultado.put("importe", 0);
            resultado.put("pujoId", null);
        }
        
        return resultado;
    }

    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getMisPujas(String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Integer clienteId = auth.getPersona().getIdentificador();
        java.util.List<Pujo> misPujos = pujoRepository.findByAsistenteClienteIdentificadorOrderByFechaHoraDesc(clienteId);

        // Agrupa por item manteniendo el orden de aparición (primero la puja más reciente)
        java.util.Map<Integer, java.util.List<Pujo>> pujosPorItem = new java.util.LinkedHashMap<>();
        for (Pujo p : misPujos) {
            Integer itemId = p.getItem().getIdentificador();
            pujosPorItem.computeIfAbsent(itemId, k -> new java.util.ArrayList<>()).add(p);
        }

        java.util.List<java.util.Map<String, Object>> resultados = new java.util.ArrayList<>();

        for (java.util.Map.Entry<Integer, java.util.List<Pujo>> entry : pujosPorItem.entrySet()) {
            Integer itemId = entry.getKey();
            java.util.List<Pujo> pujasItem = entry.getValue();
            ItemCatalogo item = pujasItem.get(0).getItem();

            // Todas las ofertas de este usuario sobre este lote, en orden cronológico
            java.util.List<BigDecimal> todasMisPujas = pujasItem.stream()
                    .sorted(java.util.Comparator.comparing(Pujo::getFechaHora))
                    .map(Pujo::getImporte)
                    .collect(java.util.stream.Collectors.toList());

            BigDecimal miMejorPuja = todasMisPujas.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            Optional<Pujo> mejor = pujoRepository.findMejorPujaByItem(itemId);

            String estadoStr;
            BigDecimal totalAPagar = BigDecimal.ZERO;
            BigDecimal comisionItem = BigDecimal.ZERO;
            BigDecimal costoEnvioItem = BigDecimal.ZERO;

            if ("si".equalsIgnoreCase(item.getSubastado())) {
                boolean gane = mejor.isPresent()
                        && mejor.get().getAsistente().getCliente().getIdentificador().equals(clienteId);
                if (gane) {
                    java.util.List<RegistroSubasta> regs = registroSubastaRepository
                            .findByProductoIdentificadorOrderByIdentificadorDesc(item.getProducto().getIdentificador());
                    if (!regs.isEmpty()) {
                        RegistroSubasta reg = regs.get(0);
                        boolean pagado = Boolean.TRUE.equals(reg.getPagado());
                        estadoStr = pagado ? "PAGADA" : "GANADA";
                        comisionItem = reg.getComision() != null ? reg.getComision() : BigDecimal.ZERO;
                        costoEnvioItem = reg.getCostoEnvio() != null ? reg.getCostoEnvio() : BigDecimal.ZERO;
                        totalAPagar = miMejorPuja.add(comisionItem).add(costoEnvioItem);
                    } else {
                        estadoStr = "GANADA";
                    }
                } else {
                    estadoStr = "PERDIDA";
                }
            } else {
                boolean ganando = mejor.isPresent()
                        && mejor.get().getAsistente().getCliente().getIdentificador().equals(clienteId);
                estadoStr = ganando ? "GANANDO" : "PERDIENDO";
            }

            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("itemId", itemId);
            map.put("productoDesc", item.getProducto().getDescripcionCompleta());
            map.put("subastaId", item.getCatalogo().getSubasta().getIdentificador());
            map.put("subastaDesc", item.getCatalogo().getSubasta().getDescripcion());
            map.put("miPuja", miMejorPuja);
            map.put("pujaActual", mejor.map(Pujo::getImporte).orElse(item.getPrecioBase()));
            map.put("estado", estadoStr);
            map.put("subastado", item.getSubastado());
            map.put("todasMisPujas", todasMisPujas);
            map.put("comision", comisionItem);
            map.put("costoEnvio", costoEnvioItem);
            map.put("totalAPagar", totalAPagar);

            resultados.add(map);
        }
        return resultados;
    }

    @Transactional
    public java.util.Map<String, Object> pagarItemGanado(Integer itemId, Long medioPagoId, String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item no encontrado: " + itemId));

        java.util.List<RegistroSubasta> registros = registroSubastaRepository
                .findByProductoIdentificadorOrderByIdentificadorDesc(item.getProducto().getIdentificador());
        if (registros.isEmpty()) {
            throw new RecursoNoEncontradoException("Registro de subasta no encontrado para el producto");
        }
        RegistroSubasta registro = registros.get(0);

        if (!registro.getCliente().getIdentificador().equals(auth.getPersona().getIdentificador())) {
            throw new PujaInvalidaException("Solo el ganador de la puja puede pagarlo.");
        }
        if (Boolean.TRUE.equals(registro.getPagado())) {
            throw new PujaInvalidaException("Este ítem ya ha sido pagado.");
        }

        // Validar y cargar el medio de pago
        MedioPago medioPago = medioPagoRepository.findById(medioPagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medio de pago no encontrado"));

        if (!medioPago.getCliente().getIdentificador().equals(auth.getPersona().getIdentificador())) {
            throw new PujaInvalidaException("El medio de pago no te pertenece.");
        }

        // Total real a cobrar: puja + comisión del catálogo + costo de envío
        BigDecimal totalAPagar = registro.getImporte()
                .add(registro.getComision() != null ? registro.getComision() : BigDecimal.ZERO)
                .add(registro.getCostoEnvio() != null ? registro.getCostoEnvio() : BigDecimal.ZERO);

        // Lógica por tipo de medio de pago
        if (medioPago.getTipo() == ar.edu.uade.grupo16.subastas.enums.TipoMedioPago.CHEQUE_CERTIFICADO) {
            BigDecimal disponible = (medioPago.getMontoCertificado() != null ? medioPago.getMontoCertificado() : BigDecimal.ZERO)
                    .subtract(medioPago.getMontoUtilizado() != null ? medioPago.getMontoUtilizado() : BigDecimal.ZERO);
            if (disponible.compareTo(totalAPagar) < 0) {
                throw new ar.edu.uade.grupo16.subastas.exception.FondosInsuficientesException(
                        String.format("Fondos insuficientes en el cheque. Disponible: $%.2f | Requerido: $%.2f (puja $%.2f + comisión $%.2f + envío $%.2f)",
                                disponible, totalAPagar,
                                registro.getImporte(),
                                registro.getComision() != null ? registro.getComision() : BigDecimal.ZERO,
                                registro.getCostoEnvio() != null ? registro.getCostoEnvio() : BigDecimal.ZERO));
            }
            // Descontar el total del saldo del cheque
            medioPago.setMontoUtilizado(
                    (medioPago.getMontoUtilizado() != null ? medioPago.getMontoUtilizado() : BigDecimal.ZERO)
                            .add(totalAPagar));
            medioPagoRepository.save(medioPago);
        }
        // TARJETA_CREDITO / CUENTA_BANCARIA: pasan siempre sin validación de saldo

        // Marcar como pagado y limpiar reserva del medio de pago
        registro.setPagado(true);
        registroSubastaRepository.save(registro);

        // Liberar solo el monto reservado (importe de la puja, sin comisión)
        medioPagoService.liberarReserva(medioPago, registro.getImporte());

        log.info("Pago confirmado — Item: {} | Cliente: {} | Tipo: {} | Importe: ${}",
                itemId, email, medioPago.getTipo(), registro.getImporte());

        return java.util.Map.of(
                "mensaje", "Pago procesado exitosamente",
                "registroId", registro.getIdentificador(),
                "importe", registro.getImporte()
        );
    }
}
