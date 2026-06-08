package tpo.g16.blackwood.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tpo.g16.blackwood.network.api.AuthApiService;
import tpo.g16.blackwood.network.api.MedioPagoApiService;

public class RetrofitClient {

    private static RetrofitClient instance;
    private final Retrofit retrofitPublic;
    private final Retrofit retrofitAuth;

    private RetrofitClient(Context context) {
        // Logging para ver requests en el Logcat
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Cliente público (Sin token)
        OkHttpClient publicClient = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        retrofitPublic = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(publicClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Interceptor para agregar el JWT
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                
                SharedPreferences prefs = context.getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
                String token = prefs.getString(ApiConfig.KEY_ACCESS_TOKEN, null);

                if (token != null) {
                    Request.Builder builder = original.newBuilder()
                            .header("Authorization", "Bearer " + token);
                    return chain.proceed(builder.build());
                }

                return chain.proceed(original);
            }
        };

        // Cliente autenticado (Con token)
        OkHttpClient authClient = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        retrofitAuth = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(authClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    public AuthApiService getAuthApiService() {
        return retrofitPublic.create(AuthApiService.class);
    }

    public MedioPagoApiService getMedioPagoApiService() {
        return retrofitAuth.create(MedioPagoApiService.class);
    }
}
