package tpo.g16.blackwood.network.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tpo.g16.blackwood.network.model.MedioPagoRequest;

public interface MedioPagoApiService {

    @POST("api/medios-pago")
    Call<Map<String, Object>> registrarMedioPago(@Body MedioPagoRequest body);

    @retrofit2.http.GET("api/medios-pago/admin/no-verificados")
    Call<java.util.List<Map<String, Object>>> getMediosPagoNoVerificados();

    @retrofit2.http.PUT("api/medios-pago/admin/{id}/verificar")
    Call<Map<String, Object>> verificarMedioPago(@retrofit2.http.Path("id") Long id);
}
