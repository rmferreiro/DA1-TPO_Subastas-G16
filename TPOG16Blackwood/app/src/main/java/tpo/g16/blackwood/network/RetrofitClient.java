package tpo.g16.blackwood.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RetrofitClient {

    public static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;
    
    // Almacenamos el token de autenticación para inyectarlo en cada petición
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Logging para poder ver las peticiones en el Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            // Interceptor para inyectar el Bearer Token en cada request
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    
                    if (authToken != null && !authToken.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + authToken);
                    }
                    
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static SubastasApiService getApiService() {
        return getClient().create(SubastasApiService.class);
    }
}
