package ar.edu.uade.grupo16.subastas.websocket;

import ar.edu.uade.grupo16.subastas.dto.websocket.AuctionUsersUpdate;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuctionPresenceListener {

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UsuarioAuthRepository usuarioAuthRepository;

    // Map: auctionId → Map<sessionId, UserInfo>
    private final Map<String, ConcurrentHashMap<String, AuctionUsersUpdate.UserInfo>>
        presenceMap = new ConcurrentHashMap<>();

    // Map: sessionId → auctionId (para saber de qué sala salir al desconectarse)
    private final Map<String, String> sessionToAuction = new ConcurrentHashMap<>();

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();
        if (destination == null) return;

        boolean isStateSub = destination.matches("/topic/auction\\..+\\.state");
        boolean isUsersSub = destination.matches("/topic/auction\\..+\\.users");

        if (!isStateSub && !isUsersSub) return;

        String auctionId = extractAuctionId(destination);

        if (isStateSub) {
            // Suscripción a .state = "join" a la sala: registrar presencia
            String sessionId = headers.getSessionId();
            String username = event.getUser() != null ? event.getUser().getName() : "Anónimo";

            AuctionUsersUpdate.UserInfo info = new AuctionUsersUpdate.UserInfo();
            info.setUsername(username);
            info.setInitials(buildInitialsFromEmail(username));

            presenceMap.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                       .put(sessionId, info);
            sessionToAuction.put(sessionId, auctionId);
        }

        // Broadcastear en ambos casos: si alguien suscribe a .users, puede haberse
        // unido justo antes de que llegara el broadcast de .state, y así recibe
        // la lista inmediatamente sin esperar la próxima acción.
        broadcastUserList(auctionId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String auctionId = sessionToAuction.remove(sessionId);

        if (auctionId != null) {
            Map<String, AuctionUsersUpdate.UserInfo> users = presenceMap.get(auctionId);
            if (users != null) {
                users.remove(sessionId);
                broadcastUserList(auctionId);
            }
        }
    }

    private void broadcastUserList(String auctionId) {
        Map<String, AuctionUsersUpdate.UserInfo> users =
            presenceMap.getOrDefault(auctionId, new ConcurrentHashMap<>());

        AuctionUsersUpdate update = new AuctionUsersUpdate();
        update.setCount(users.size());
        update.setParticipants(new ArrayList<>(users.values()));

        messagingTemplate.convertAndSend(
            "/topic/auction." + auctionId + ".users", update
        );
    }

    private String extractAuctionId(String destination) {
        // "/topic/auction.42.state" → "42"
        // "/topic/auction.42.users" → "42"
        String[] parts = destination.split("\\.");
        return parts.length >= 2 ? parts[1] : destination;
    }

    /**
     * Busca el nombre real del usuario por email y calcula sus iniciales.
     * Fallback: primeras 2 letras del email.
     */
    private String buildInitialsFromEmail(String email) {
        if (email == null || email.isBlank()) return "?";
        try {
            var authOpt = usuarioAuthRepository.findByEmail(email);
            if (authOpt.isPresent() && authOpt.get().getPersona() != null) {
                String nombre = authOpt.get().getPersona().getNombre();
                if (nombre != null && !nombre.isBlank()) {
                    return buildInitials(nombre);
                }
            }
        } catch (Exception ignored) {}
        // Fallback: primeras 2 letras del email
        return email.substring(0, Math.min(2, email.length())).toUpperCase();
    }

    private String buildInitials(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String clean = nombre.trim();
        return clean.substring(0, Math.min(2, clean.length())).toUpperCase();
    }
}
