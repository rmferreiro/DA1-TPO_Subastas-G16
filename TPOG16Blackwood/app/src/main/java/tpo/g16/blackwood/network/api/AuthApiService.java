package tpo.g16.blackwood.network.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tpo.g16.blackwood.network.model.AuthResponse;
import tpo.g16.blackwood.network.model.RegistroRequest;
// import tpo.g16.blackwood.network.model.LoginRequest; // Cuando lo agreguemos

public interface AuthApiService {

    @POST("api/auth/registro")
    Call<AuthResponse> registrar(@Body RegistroRequest body);

    /*
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);
    */
}
