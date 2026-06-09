import os

content = """package tpo.g16.blackwood.network;

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

    public static final String BASE_URL = "http://10.0.2.2:8080/";
    
    private static RetrofitClient instance;
    private final Retrofit retrofitPublic;
    private final Retrofit retrofitAuth;

    private RetrofitClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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

    // ============================================
    // OLD STATIC APPROACH (HEAD)
    // ============================================
    private static Retrofit retrofit = null;
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

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
"""

with open("app/src/main/java/tpo/g16/blackwood/network/RetrofitClient.java", "w", encoding="utf-8") as f:
    f.write(content)

print("RetrofitClient rewritten.")
