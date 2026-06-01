package ar.edu.uade.grupo16.subastas.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret = "clave-secreta-subastas-uade-g16-2026-esta-clave-debe-tener-al-menos-256-bits";
    private long accessTokenExpiration = 3600000;   // 1 hora en ms
    private long refreshTokenExpiration = 604800000; // 7 días en ms
}
