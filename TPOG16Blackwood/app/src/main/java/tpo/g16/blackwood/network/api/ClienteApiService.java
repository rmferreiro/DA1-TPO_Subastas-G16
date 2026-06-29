package tpo.g16.blackwood.network.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Endpoints admin para la gestión de clientes/usuarios pendientes de verificación.
 * Requieren rol ADMIN en el backend (token JWT enviado automáticamente por RetrofitClient).
 */
public interface ClienteApiService {

    /** Devuelve la lista de clientes en estado ESPERANDO_APROBACION. */
    @GET("api/auth/registro/pendientes")
    Call<List<Map<String, Object>>> getUsuariosPendientes();

    /**
     * Aprueba el registro de un usuario.
     * Body: { "email": "...", "categoria": "bronce"|"plata"|"oro" }
     * Pasa al usuario de ESPERANDO_APROBACION → APROBADO para que pueda crear su contraseña.
     */
    @POST("api/auth/registro/aprobar")
    Call<Map<String, Object>> aprobarUsuario(@Body Map<String, Object> request);
}
