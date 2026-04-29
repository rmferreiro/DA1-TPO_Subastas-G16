package ar.edu.uade.grupo16.subastas.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String nombre;
    private String categoria;
    private String estado;
    @Builder.Default
    private String tokenType = "Bearer";
}
