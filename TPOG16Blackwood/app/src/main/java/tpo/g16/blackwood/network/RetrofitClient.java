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

    public static final String BASE_URL = ApiConfig.BASE_URL;

    private static RetrofitClient instance;
    private static Context appContext = null;
    private final Retrofit retrofitPublic;
    private final Retrofit retrofitAuth;

    private RetrofitClient(Context context) {
        appContext = context.getApplicationContext();
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
                    Response response = chain.proceed(builder.build());
                    if (response.code() == 401) {
                        manejarSesionVencida();
                    }
                    return response;
                }

                Response response = chain.proceed(original);
                if (response.code() == 401 && prefs.getString(ApiConfig.KEY_ACCESS_TOKEN, null) != null) {
                    manejarSesionVencida();
                }
                return response;
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
        } else {
            appContext = context.getApplicationContext();
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

    private static boolean isRedirecting = false;

    private static void manejarSesionVencida() {
        if (appContext == null) return;
        synchronized (RetrofitClient.class) {
            if (isRedirecting) return;
            isRedirecting = true;
        }

        // Limpiar preferences
        SharedPreferences prefs = appContext.getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        authToken = null;

        // Redirigir a LoginActivity
        android.content.Intent intent = new android.content.Intent(appContext, tpo.g16.blackwood.login.LoginActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        appContext.startActivity(intent);

        // Mostrar Toast
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            android.widget.Toast.makeText(appContext, "Sesión vencida. Vuelve a ingresar por favor", android.widget.Toast.LENGTH_LONG).show();
            synchronized (RetrofitClient.class) {
                isRedirecting = false;
            }
        });
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

                    String token = authToken;
                    if (token == null && appContext != null) {
                        SharedPreferences prefs = appContext.getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
                        token = prefs.getString(ApiConfig.KEY_ACCESS_TOKEN, null);
                    }

                    if (token != null && !token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    Request request = requestBuilder.build();
                    Response response = chain.proceed(request);
                    if (response.code() == 401 && token != null) {
                        manejarSesionVencida();
                    }
                    return response;
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
