package tpo.g16.blackwood.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import tpo.g16.blackwood.network.models.SubastaResponse;
import tpo.g16.blackwood.network.models.LoginRequest;
import tpo.g16.blackwood.network.models.AuthResponse;

public interface SubastasApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("api/subastas")
    Call<List<SubastaResponse>> getSubastasDisponibles();

    @GET("api/subastas/{id}")
    Call<SubastaResponse> getSubastaById(@Path("id") int id);

    /** Estado completo de la subasta en vivo: item activo, mejor oferta, límites. */
    @GET("api/subastas/{id}/estado-vivo")
    Call<Map<String, Object>> getEstadoVivo(@Path("id") int id);

    @POST("api/subastas/{id}/unirse")
    Call<Map<String, Object>> unirseSubasta(@Path("id") int id);

    @DELETE("api/subastas/{id}/salir")
    Call<Map<String, Object>> salirSubasta(@Path("id") int id);

    @GET("api/productos/catalogo/{subastaId}")
    Call<List<Map<String, Object>>> getCatalogo(@Path("subastaId") int subastaId);

    @GET("api/productos/catalogo/{subastaId}/disponibles")
    Call<List<Map<String, Object>>> getItemsDisponibles(@Path("subastaId") int subastaId);

    @GET("api/productos/catalogo/items/{itemId}")
    Call<Map<String, Object>> getItemDetalle(@Path("itemId") int itemId);

    @POST("api/pujas/items/{itemId}/pagar")
    Call<Map<String, Object>> pagarItemGanado(@Path("itemId") int itemId, @Body Map<String, Object> request);

    @GET("api/medios-pago")
    Call<List<Map<String, Object>>> getMediosPago();

    @POST("api/medios-pago")
    Call<Map<String, Object>> agregarMedioPago(@Body Map<String, Object> request);

    @PUT("api/medios-pago/{id}")
    Call<Map<String, Object>> actualizarMedioPago(@Path("id") Long id, @Body Map<String, Object> request);

    @DELETE("api/medios-pago/{id}")
    Call<Map<String, Object>> eliminarMedioPago(@Path("id") Long id);

    @GET("api/pujas/mis-pujas")
    Call<List<tpo.g16.blackwood.network.models.MiPuja>> getMisPujas();

    @GET("api/clientes/perfil")
    Call<tpo.g16.blackwood.network.models.ClienteResponse> getPerfil();

    @GET("api/clientes/metricas")
    Call<Map<String, Object>> getMetricas();

    @GET("api/productos/mis-productos")
    Call<List<Map<String, Object>>> getMisProductos();

    @POST("api/productos/solicitar")
    Call<Map<String, Object>> solicitarProducto(@Body Map<String, Object> request);

    @GET("api/productos/{id}")
    Call<Map<String, Object>> getProductoDetalle(@Path("id") int id);

    @GET("api/productos/pendientes")
    Call<List<Map<String, Object>>> getProductosPendientes();

    @PUT("api/productos/{id}/revisar")
    Call<Map<String, Object>> revisarProducto(@Path("id") int id, @Body Map<String, Object> request);

    @PUT("api/productos/{id}/condiciones-duenio")
    Call<Map<String, Object>> responderCondiciones(@Path("id") int id, @Body Map<String, Object> request);

    @POST("api/subastas")
    Call<Map<String, Object>> crearSubasta(@Body Map<String, Object> request);

    @GET("api/productos/aprobados")
    Call<List<Map<String, Object>>> getProductosAprobados();

    // ── Multas ──────────────────────────────────────────────────────────────

    /** Multas pendientes de pago del usuario autenticado. */
    @GET("api/multas/pendientes")
    Call<List<Map<String, Object>>> getMultasPendientes();

    /** Todas las multas (pagas y pendientes) del usuario autenticado. */
    @GET("api/multas")
    Call<List<Map<String, Object>>> getTodasMultas();

    /** Pagar una multa por su id. */
    @POST("api/multas/{id}/pagar")
    Call<Map<String, Object>> pagarMulta(@Path("id") long id);
}
