package tpo.g16.blackwood.network.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tpo.g16.blackwood.network.model.AuthResponse;
import tpo.g16.blackwood.network.model.RegistroRequest;
import tpo.g16.blackwood.network.model.LoginRequest;

public interface AuthApiService {

    @POST("api/auth/registro")
    Call<AuthResponse> registrar(@Body RegistroRequest body);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    @retrofit2.http.GET("api/auth/registro/estado")
    Call<java.util.Map<String, Object>> obtenerEstadoRegistro(@retrofit2.http.Query("email") String email);

    @POST("api/auth/registro/completar")
    Call<AuthResponse> completarRegistro(@Body tpo.g16.blackwood.network.model.CompletarRegistroRequest body);
}
