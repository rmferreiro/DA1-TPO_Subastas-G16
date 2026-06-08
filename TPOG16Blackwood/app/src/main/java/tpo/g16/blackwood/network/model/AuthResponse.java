package tpo.g16.blackwood.network.model;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String nombre;
    private String categoria;
    private String estado;
    private String tokenType;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getEstado() { return estado; }
    public String getTokenType() { return tokenType; }
}
