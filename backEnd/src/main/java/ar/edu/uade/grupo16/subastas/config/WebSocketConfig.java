package ar.edu.uade.grupo16.subastas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para topics de broadcast (ej: /topic/subasta/1)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes del cliente al servidor (ej: /app/pujar)
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para mensajes privados a un usuario específico
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal de WebSocket
        registry.addEndpoint("/ws/subasta")
                .setAllowedOrigins("*");

        // Mismo endpoint con fallback SockJS (para clientes que no soporten WS nativo)
        registry.addEndpoint("/ws/subasta")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
