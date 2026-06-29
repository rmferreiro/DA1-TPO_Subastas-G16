package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.request.PujaRequest;
import ar.edu.uade.grupo16.subastas.dto.response.PujaResponse;
import ar.edu.uade.grupo16.subastas.dto.websocket.AuctionStateUpdate;
import ar.edu.uade.grupo16.subastas.dto.websocket.BidError;
import ar.edu.uade.grupo16.subastas.dto.websocket.BidRequest;
import ar.edu.uade.grupo16.subastas.entity.Cliente;
import ar.edu.uade.grupo16.subastas.entity.ItemCatalogo;
import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.entity.Pujo;
import ar.edu.uade.grupo16.subastas.entity.Subasta;
import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.repository.*;
import ar.edu.uade.grupo16.subastas.service.PujaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuctionBidController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PujaService pujaService;
    private final SubastaRepository subastaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ClienteRepository clienteRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final PujoRepository pujoRepository;

    /**
     * Un lock por subastaId para serializar pujas concurrentes del mismo lote.
     * Evita condiciones de carrera cuando múltiples usuarios pujan simultáneamente
     * vía WebSocket sin pasar por el SubastaSalaManager.
     */
    private final ConcurrentHashMap<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    private ReentrantLock getLock(Integer auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, id -> new ReentrantLock(true));
    }

    @MessageMapping("/auction.{auctionId}.bid")
    public void handleBid(
            @DestinationVariable Integer auctionId,
            @Payload BidRequest bid,
            Principal principal
    ) {
        if (principal == null) {
            log.warn("Bid rejected: principal is null");
            return;
        }

        String username = principal.getName();
        log.info("Received bid for auction {} from user {}: amount {}", auctionId, username, bid.getAmount());

        // Serializar pujas de la misma subasta para evitar condición de carrera
        ReentrantLock lock = getLock(auctionId);
        lock.lock();

        ItemCatalogo item = null;
        Subasta subasta = null;

        try {
            // 1. Cargar subasta
            subasta = subastaRepository.findById(auctionId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

            // 2. Obtener item actual de la subasta
            Integer itemId = subasta.getItemActualId();
            if (itemId == null) {
                throw new IllegalArgumentException("No hay ningún lote activo en esta subasta");
            }

            item = itemCatalogoRepository.findById(itemId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Item actual no encontrado"));

            // 3. Buscar cliente por username (email)
            UsuarioAuth auth = usuarioAuthRepository.findByEmail(username)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

            // 4. Buscar medio de pago verificado y activo del cliente (compatible con la moneda de la subasta)
            Moneda monedaSubasta = subasta.getMoneda() != null ? subasta.getMoneda() : Moneda.ARS;
            List<MedioPago> mediosPago = medioPagoRepository
                    .findByClienteIdentificadorAndMonedaAndVerificadoTrueAndActivoTrue(
                            cliente.getIdentificador(), monedaSubasta);

            if (mediosPago.isEmpty()) {
                mediosPago = medioPagoRepository
                        .findByClienteIdentificadorAndVerificadoTrueAndActivoTrue(cliente.getIdentificador());
            }

            if (mediosPago.isEmpty()) {
                throw new IllegalArgumentException("No tenés ningún medio de pago verificado y activo en el sistema.");
            }

            Long medioPagoId = mediosPago.get(0).getId();

            // 5. Procesar puja usando la lógica reglamentaria centralizada en PujaService
            PujaRequest request = new PujaRequest(itemId, BigDecimal.valueOf(bid.getAmount()), medioPagoId);
            PujaResponse response = pujaService.procesarPuja(auctionId, request, username);

            // 6. Broadcast del nuevo estado del lote a todos los participantes
            AuctionStateUpdate update = AuctionStateUpdate.builder()
                    .currentPrice(response.getImporte().doubleValue())
                    .topBidderName(response.getNombrePostor())
                    .endEpochMillis(response.getLimiteFinalizacionEpoch())
                    .lotNumber(itemId)
                    .build();

            messagingTemplate.convertAndSend("/topic/auction." + auctionId + ".state", update);

        } catch (Exception e) {
            log.error("Error processing WebSocket bid from {}: {}", username, e.getMessage());
            double minimumRequired = calculateMinimumRequired(subasta, item);
            BidError error = BidError.builder()
                    .reason(e.getMessage())
                    .minimumRequired(minimumRequired)
                    .build();
            messagingTemplate.convertAndSendToUser(username, "/queue/bid_error", error);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Calcula el mínimo requerido para la siguiente puja, usada para informar
     * al cliente en caso de error. Respeta la regla del enunciado: para Oro/Platino
     * solo se requiere superar la oferta actual (mínimo simbólico +1).
     */
    private double calculateMinimumRequired(Subasta subasta, ItemCatalogo item) {
        if (item == null) return 0.0;
        Optional<Pujo> mejorPujaActual = pujoRepository.findMejorPujaByItem(item.getIdentificador());
        BigDecimal precioBase = item.getPrecioBase();

        String catSubasta = (subasta != null && subasta.getCategoria() != null)
                ? subasta.getCategoria().toLowerCase() : "";
        boolean sinLimites = catSubasta.equals("oro") || catSubasta.equals("platino");

        if (mejorPujaActual.isPresent()) {
            BigDecimal mejorOferta = mejorPujaActual.get().getImporte();
            if (sinLimites) {
                // Oro/Platino: cualquier valor que supere la oferta actual
                return mejorOferta.add(BigDecimal.ONE).doubleValue();
            }
            return mejorOferta.add(precioBase.multiply(new BigDecimal("0.01"))).doubleValue();
        }
        return precioBase.doubleValue();
    }
}
