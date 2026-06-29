package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.response.PujaResponse;
import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubastaScheduler {

    private final SubastaRepository subastaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ClienteRepository clienteRepository;
    private final AsistenteRepository asistenteRepository;
    private final PujoRepository pujoRepository;
    private final PujaService pujaService;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelay = 10000)
    public void activarSubastas() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Subasta> programadas = subastaRepository.findByEstado("PENDIENTE");

        for (Subasta subasta : programadas) {
            boolean debeIniciar = false;
            if (subasta.getFecha().isBefore(hoy)) {
                debeIniciar = true;
            } else if (subasta.getFecha().isEqual(hoy) && !subasta.getHora().isAfter(ahora)) {
                debeIniciar = true;
            }

            if (debeIniciar) {
                log.info("Activando subasta ID {} ({}) programada para {} {}", subasta.getIdentificador(), subasta.getEstado(), subasta.getFecha(), subasta.getHora());
                try {
                    transactionTemplate.executeWithoutResult(status -> activarSubastaInterno(subasta));
                } catch (Exception e) {
                    log.error("Error al activar subasta ID {}: {}", subasta.getIdentificador(), e.getMessage(), e);
                }
            }
        }
    }

    private void activarSubastaInterno(Subasta subasta) {
        // 1. Obtener cliente Blackwood Subastas (ID 9999 o documento BLACKWOOD_SUBASTAS)
        Cliente blackwood = clienteRepository.findById(9999)
                .orElseGet(() -> clienteRepository.findByPersonaDocumento("BLACKWOOD_SUBASTAS")
                        .orElseThrow(() -> new IllegalStateException("Cliente 'Blackwood Subastas' no encontrado en el sistema. Debe ejecutarse el seed de base de datos.")));

        // 2. Registrar como Asistente en la subasta si no existe
        Asistente asistente = asistenteRepository
                .findByClienteIdentificadorAndSubastaIdentificador(blackwood.getIdentificador(), subasta.getIdentificador())
                .orElseGet(() -> {
                    Asistente nuevoAsistente = Asistente.builder()
                            .cliente(blackwood)
                            .subasta(subasta)
                            .numeroPostor(999)
                            .build();
                    return asistenteRepository.save(nuevoAsistente);
                });

        // 3. Registrar Puja Original para cada item del catálogo de la subasta
        List<ItemCatalogo> items = itemCatalogoRepository
                .findByCatalogoSubastaIdentificador(subasta.getIdentificador());

        Integer primerItemId = null;
        for (ItemCatalogo item : items) {
            // Registrar pujo inicial si no hay pujas aún
            boolean tienePujas = pujoRepository.findMejorPujaByItem(item.getIdentificador()).isPresent();
            if (!tienePujas) {
                Pujo pujaInicial = Pujo.builder()
                        .asistente(asistente)
                        .item(item)
                        .importe(item.getPrecioBase())
                        .ganador("si")
                        .fechaHora(LocalDateTime.now())
                        .build();
                pujoRepository.save(pujaInicial);
                log.info("Registrada puja inicial (Blackwood Subastas) de ${} para el Item ID {}", item.getPrecioBase(), item.getIdentificador());
            }

            if (item.getOrden() != null && item.getOrden() == 1) {
                primerItemId = item.getIdentificador();
            } else if (primerItemId == null) {
                primerItemId = item.getIdentificador();
            }
        }

        // 4. Cambiar estado y configurar tiempo límite inicial de 5 minutos
        subasta.setEstado("ACTIVA");
        subasta.setItemActualId(primerItemId);
        subasta.setLimiteFinalizacionEpoch(Instant.now().toEpochMilli() + 300000); // 5 minutos

        subastaRepository.save(subasta);
        log.info("Subasta ID {} activada con éxito. Expira en 5 minutos.", subasta.getIdentificador());
    }

    /**
     * Tarea 2: Manejar la expiración del lote actual. Si vence el tiempo (5 minutos),
     * se cierra el lote actual y se pasa al siguiente. Si no hay más lotes, se finaliza la subasta.
     * Se ejecuta cada 10 segundos.
     */
    @Scheduled(fixedDelay = 10000)
    public void finalizarSubastasExpiradas() {
        long ahora = Instant.now().toEpochMilli();

        List<Subasta> abiertas = subastaRepository.findByEstado("ACTIVA");

        for (Subasta subasta : abiertas) {
            try {
                if (subasta.getLimiteFinalizacionEpoch() == null || subasta.getItemActualId() == null) {
                    inicializarSubastaNula(subasta);
                } else if (subasta.getLimiteFinalizacionEpoch() <= ahora) {
                    log.info("Lote/Subasta ID {} expiró (tiempo: {} <= ahora: {}). Procesando siguiente paso.",
                            subasta.getIdentificador(), subasta.getLimiteFinalizacionEpoch(), ahora);
                    procesarVencimientoLote(subasta);
                }
            } catch (Exception e) {
                log.error("Error al procesar vencimiento de lote para subasta ID {}: {}", subasta.getIdentificador(), e.getMessage(), e);
            }
        }
    }

    private void inicializarSubastaNula(Subasta subasta) {
        transactionTemplate.executeWithoutResult(status -> {
            Subasta sub = subastaRepository.findById(subasta.getIdentificador()).orElse(null);
            if (sub != null) {
                inicializarSubastaNulaInterno(sub);
            }
        });
    }

    private void inicializarSubastaNulaInterno(Subasta sub) {
        log.info("Inicializando campos de tiempo nulos para subasta abierta ID {}", sub.getIdentificador());
        List<ItemCatalogo> items = itemCatalogoRepository
                .findByCatalogoSubastaIdentificador(sub.getIdentificador());
        Integer primerItemId = null;
        for (ItemCatalogo item : items) {
            if (item.getOrden() != null && item.getOrden() == 1) {
                primerItemId = item.getIdentificador();
            } else if (primerItemId == null) {
                primerItemId = item.getIdentificador();
            }
        }
        sub.setItemActualId(primerItemId);
        sub.setLimiteFinalizacionEpoch(Instant.now().toEpochMilli() + 300000); // 5 minutos
        subastaRepository.save(sub);
        
        // Forzar también registro de puja base de Blackwood si no tiene
        if (primerItemId != null) {
            try {
                activarSubastaInterno(sub);
            } catch (Exception ignored) {}
        }
    }

    /** Resultado de procesarVencimientoLoteInterno: datos a broadcastear DESPUÉS del commit. */
    private static class VencimientoResult {
        final Integer auctionId;
        /** LotChangeUpdate o AuctionFinishedUpdate, null si hubo un error no recuperable. */
        final Object broadcast;

        VencimientoResult(Integer auctionId, Object broadcast) {
            this.auctionId = auctionId;
            this.broadcast = broadcast;
        }
    }

    private void procesarVencimientoLote(Subasta subasta) {
        // Holder: el broadcast se construye DENTRO de la transacción (con los datos necesarios)
        // pero se envía DESPUÉS del commit para garantizar consistencia.
        final VencimientoResult[] resultHolder = new VencimientoResult[1];

        transactionTemplate.executeWithoutResult(status -> {
            Subasta sub = subastaRepository.findById(subasta.getIdentificador()).orElse(null);
            if (sub != null) {
                resultHolder[0] = procesarVencimientoLoteInterno(sub);
            }
        });

        // Broadcast DESPUÉS del commit — el cliente verá datos ya persistidos
        VencimientoResult result = resultHolder[0];
        if (result == null || result.broadcast == null) return;

        String topic;
        if (result.broadcast instanceof ar.edu.uade.grupo16.subastas.dto.websocket.LotChangeUpdate) {
            topic = "/topic/auction." + result.auctionId + ".lot_change";
        } else {
            topic = "/topic/auction." + result.auctionId + ".finished";
        }
        try {
            messagingTemplate.convertAndSend(topic, result.broadcast);
            log.info("Broadcast enviado a {} para subasta {}", topic, result.auctionId);
        } catch (Exception e) {
            log.error("Error al broadcastear cambio de lote/fin para subasta {}: {}", result.auctionId, e.getMessage());
        }
    }

    /**
     * Ejecuta la transición de lote DENTRO de una transacción activa.
     * NO broadcastea — devuelve los datos de broadcast para que el caller los envíe
     * DESPUÉS del commit, garantizando que el cliente vea el estado ya persistido.
     */
    private VencimientoResult procesarVencimientoLoteInterno(Subasta subasta) {
        Integer itemActualId = subasta.getItemActualId();

        // 1. Email del subastador para auditoría
        String emailSubastador = "admin@gmail.com";
        if (subasta.getSubastador() != null && subasta.getSubastador().getPersona() != null) {
            emailSubastador = usuarioAuthRepository
                    .findByPersonaIdentificador(subasta.getSubastador().getPersona().getIdentificador())
                    .map(UsuarioAuth::getEmail)
                    .orElse("admin@gmail.com");
        }

        // 2. Cerrar el item actual (siempre intentar, nunca bloquear la transición)
        String winnerName = "Nadie";
        double finalPrice = 0.0;
        int soldLotOrder = 0;
        if (itemActualId != null) {
            Optional<ItemCatalogo> itemOpt = itemCatalogoRepository.findById(itemActualId);
            if (itemOpt.isPresent()) {
                ItemCatalogo itemActual = itemOpt.get();
                soldLotOrder = itemActual.getOrden() != null ? itemActual.getOrden() : 0;
                if (!"si".equalsIgnoreCase(itemActual.getSubastado())) {
                    try {
                        pujaService.cerrarItem(subasta.getIdentificador(), itemActual.getIdentificador(), emailSubastador);
                        log.info("Lote actual ID {} cerrado/vendido.", itemActual.getIdentificador());
                    } catch (Exception e) {
                        log.warn("No se pudo cerrar formalmente el Item ID {}: {}. Se marca como subastado de todos modos.",
                                itemActual.getIdentificador(), e.getMessage());
                    }
                    // CRÍTICO: actualizar cache JPA del outer EntityManager SIEMPRE,
                    // independientemente del resultado de cerrarItem (REQUIRES_NEW).
                    // Evita que findByCatalogoSubastaIdentificador devuelva este item
                    // como "no subastado" desde el cache de primer nivel.
                    itemActual.setSubastado("si");
                    itemCatalogoRepository.save(itemActual);
                }

                Optional<Pujo> ganador = pujoRepository.findGanadorByItem(itemActual.getIdentificador());
                if (ganador.isPresent()) {
                    winnerName = ganador.get().getAsistente().getCliente().getPersona().getNombre();
                    finalPrice = ganador.get().getImporte().doubleValue();
                } else {
                    finalPrice = itemActual.getPrecioBase().doubleValue();
                }
            }
        }

        // 3. Buscar todos los items ordenados por orden/identificador
        List<ItemCatalogo> items = itemCatalogoRepository
                .findByCatalogoSubastaIdentificador(subasta.getIdentificador())
                .stream()
                .sorted((a, b) -> {
                    int oa = a.getOrden() != null ? a.getOrden() : a.getIdentificador();
                    int ob = b.getOrden() != null ? b.getOrden() : b.getIdentificador();
                    return Integer.compare(oa, ob);
                })
                .toList();

        // 4. Primer item no subastado = siguiente lote
        ItemCatalogo siguienteItem = null;
        for (ItemCatalogo item : items) {
            if (!"si".equalsIgnoreCase(item.getSubastado())) {
                siguienteItem = item;
                break;
            }
        }

        if (siguienteItem != null) {
            // ── Caso A: hay un siguiente lote ──────────────────────────────────
            log.info("Pasando al siguiente lote ID {} (orden {}) en la subasta ID {}",
                    siguienteItem.getIdentificador(),
                    siguienteItem.getOrden(),
                    subasta.getIdentificador());

            // Registrar puja inicial de Blackwood para el nuevo lote (no crítico).
            // Si falla por cualquier razón, el lote avanza igual.
            try {
                boolean tienePujas = pujoRepository.findMejorPujaByItem(siguienteItem.getIdentificador()).isPresent();
                if (!tienePujas) {
                    Cliente blackwood = clienteRepository.findById(9999)
                            .orElseGet(() -> clienteRepository
                                    .findByPersonaDocumento("BLACKWOOD_SUBASTAS")
                                    .orElse(null));
                    if (blackwood != null) {
                        Asistente asistente = asistenteRepository
                                .findByClienteIdentificadorAndSubastaIdentificador(
                                        blackwood.getIdentificador(), subasta.getIdentificador())
                                .orElseGet(() -> asistenteRepository.save(
                                        Asistente.builder()
                                                .cliente(blackwood)
                                                .subasta(subasta)
                                                .numeroPostor(999)
                                                .build()));
                        pujoRepository.save(Pujo.builder()
                                .asistente(asistente)
                                .item(siguienteItem)
                                .importe(siguienteItem.getPrecioBase())
                                .ganador("si")
                                .fechaHora(LocalDateTime.now())
                                .build());
                        log.info("Puja inicial Blackwood ${} registrada para Item ID {}",
                                siguienteItem.getPrecioBase(), siguienteItem.getIdentificador());
                    } else {
                        log.warn("Cliente Blackwood no encontrado. El lote {} avanza sin puja inicial.",
                                siguienteItem.getIdentificador());
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo registrar puja inicial para Item ID {}: {}. El lote avanza de todos modos.",
                        siguienteItem.getIdentificador(), e.getMessage());
            }

            // Actualizar subasta → nuevo lote, timer reiniciado
            subasta.setItemActualId(siguienteItem.getIdentificador());
            long nuevoEpoch = Instant.now().toEpochMilli() + 300_000L;
            subasta.setLimiteFinalizacionEpoch(nuevoEpoch);
            subastaRepository.save(subasta);

            ar.edu.uade.grupo16.subastas.dto.websocket.LotChangeUpdate broadcast =
                    ar.edu.uade.grupo16.subastas.dto.websocket.LotChangeUpdate.builder()
                            .soldLotNumber(itemActualId != null ? itemActualId : 0)
                            .soldLotOrder(soldLotOrder)
                            .soldLotWinnerName(winnerName)
                            .soldLotFinalPrice(finalPrice)
                            .newLotNumber(siguienteItem.getIdentificador())
                            .newLotOrder(siguienteItem.getOrden() != null ? siguienteItem.getOrden() : 0)
                            .newLotTitle(siguienteItem.getProducto().getDescripcionCompleta())
                            .newLotDescription(siguienteItem.getProducto().getDescripcionCatalogo())
                            .newLotImageUrl("/api/productos/" + siguienteItem.getProducto().getIdentificador() + "/foto")
                            .newLotStartingPrice(siguienteItem.getPrecioBase().doubleValue())
                            .newLotStartingBidder("Blackwood Subastas")
                            .newLotEndEpochMillis(nuevoEpoch)
                            .build();

            return new VencimientoResult(subasta.getIdentificador(), broadcast);

        } else {
            // ── Caso B: no hay más lotes, finalizar subasta ────────────────────
            log.info("No hay más lotes disponibles. Finalizando subasta ID {}.", subasta.getIdentificador());
            subasta.setEstado("FINALIZADA");
            subasta.setItemActualId(null);
            subasta.setLimiteFinalizacionEpoch(null);
            subastaRepository.save(subasta);

            ar.edu.uade.grupo16.subastas.dto.websocket.AuctionFinishedUpdate broadcast =
                    ar.edu.uade.grupo16.subastas.dto.websocket.AuctionFinishedUpdate.builder()
                            .message("Subasta finalizada, gracias por participar")
                            .build();

            return new VencimientoResult(subasta.getIdentificador(), broadcast);
        }
    }
}
