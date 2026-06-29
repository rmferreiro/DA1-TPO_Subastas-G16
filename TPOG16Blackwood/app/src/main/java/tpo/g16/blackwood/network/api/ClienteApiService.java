package tpo.g16.blackwood.network.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Endpoints admin para la gestión de usuarios pendientes de aprobación.
 * Requieren autenticación (token JWT enviado automáticamente por RetrofitClient).
 */
public interface ClienteApiService {

    /** Devuelve usuarios con estado PENDIENTE. */
    @GET("api/admin/usuarios/pendientes")
    Call<List<Map<String, Object>>> getUsuariosPendientes();

    /**
     * Aprueba un usuario por UUID.
     * Body: { "categoria": "comun"|"especial"|"plata"|"oro"|"platino" }
     */
    @PUT("api/admin/usuarios/{uuid}/aprobar")
    Call<Map<String, Object>> aprobarUsuario(
            @Path("uuid") String uuid,
            @Body Map<String, Object> request);
}
