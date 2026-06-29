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
import ar.edu.uade.grupo16.subastas.repository.ClienteRepository;
import ar.edu.uade.grupo16.subastas.repository.ItemCatalogoRepository;
import ar.edu.uade.grupo16.subastas.repository.PujoRepository;
import ar.edu.uade.grupo16.subastas.repository.SubastaRepository;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import ar.edu.uade.grupo16.subastas.service.MedioPagoService;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuctionBidController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PujaService pujaService;
    private final MedioPagoService medioPagoService;
    private final SubastaRepository subastaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ClienteRepository clienteRepository;
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
        log.info("Received bid for auction {} from user {}: amount {} mpId={}",
                auctionId, username, bid.getAmount(), bid.getMedioPagoId());

        ReentrantLock lock = getLock(auctionId);
        lock.lock();

        ItemCatalogo item = null;
        Subasta subasta = null;

        try {
            subasta = subastaRepository.findById(auctionId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

            Integer itemId = subasta.getItemActualId();
            if (itemId == null) {
                throw new IllegalArgumentException("No hay ningún lote activo en esta subasta");
            }

            item = itemCatalogoRepository.findById(itemId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Item actual no encontrado"));

            UsuarioAuth auth = usuarioAuthRepository.findByEmail(username)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

            Moneda monedaSubasta = subasta.getMoneda() != null ? subasta.getMoneda() : Moneda.ARS;
            MedioPago medioPago = medioPagoService.resolverMedioPagoParaPuja(
                    cliente.getIdentificador(), monedaSubasta, bid.getMedioPagoId());
            Long medioPagoId = medioPago.getId();

            PujaRequest request = new PujaRequest(itemId, BigDecimal.valueOf(bid.getAmount()), medioPagoId);
            PujaResponse response = pujaService.procesarPuja(auctionId, request, username);

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
                return mejorOferta.add(BigDecimal.ONE).doubleValue();
            }
            return mejorOferta.add(precioBase.multiply(new BigDecimal("0.01"))).doubleValue();
        }
        return precioBase.doubleValue();
    }
}
