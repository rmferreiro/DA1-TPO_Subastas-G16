package tpo.g16.blackwood.network;

public final class ApiConfig {
    // ─────────────────────────────────────────────────────────────────────────
    // AMBIENTE — cambiar para apuntar a distintos entornos
    // Emulador Android: 10.0.2.2 es el alias del host
    // Dispositivo físico: reemplazar por la IP de PC en la red local
    // ─────────────────────────────────────────────────────────────────────────
    public static final String BASE_URL = "http://192.168.1.8:8080/";

    // Timeouts
    public static final int CONNECT_TIMEOUT_SECONDS = 15;
    public static final int READ_TIMEOUT_SECONDS    = 60;
    public static final int WRITE_TIMEOUT_SECONDS   = 60;

    // Clave para guardar el JWT en SharedPreferences
    public static final String PREFS_NAME         = "blackwood_prefs";
    public static final String KEY_ACCESS_TOKEN   = "access_token";
    public static final String KEY_REFRESH_TOKEN  = "refresh_token";
    public static final String KEY_USER_EMAIL     = "user_email";
    public static final String KEY_USER_NOMBRE    = "user_nombre";
    public static final String KEY_USER_CATEGORIA = "user_categoria";

    private ApiConfig() {}
}
