package tpo.g16.blackwood.network.models;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String nombre;
    private String categoria;
    private String estado;
    private String tokenType;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    // Add other getters if needed
}
