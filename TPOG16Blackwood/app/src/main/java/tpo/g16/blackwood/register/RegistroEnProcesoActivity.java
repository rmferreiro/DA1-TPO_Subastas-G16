package tpo.g16.blackwood.register;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.network.ApiConfig;
import tpo.g16.blackwood.network.RetrofitClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroEnProcesoActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private String email;
    private int pollingCount = 0;

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            verificarEstado();
            
            // Polling adaptativo para minimizar recursos
            long delay;
            if (pollingCount < 36) {          // Primeros 3 minutos: cada 5s
                delay = 5000;
            } else if (pollingCount < 48) {   // Siguientes 3 minutos: cada 15s
                delay = 15000;
            } else {                           // Estabilizado a partir de los 6 minutos: cada 30s
                delay = 30000;
            }
            
            pollingCount++;
            handler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_en_proceso);

        // Obtener email desde el intent o desde SharedPreferences (para recuperarse si la app se cerró)
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
            email = prefs.getString(ApiConfig.KEY_REGISTRATION_EMAIL, null);
        }

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Error de inconsistencia de registro", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reiniciar contador de intentos al entrar a primer plano
        pollingCount = 0;
        // Iniciar polling
        handler.post(pollingRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener polling inmediatamente al salir para no consumir recursos en segundo plano
        handler.removeCallbacks(pollingRunnable);
    }

    private void verificarEstado() {
        RetrofitClient.getInstance(this).getAuthApiService()
                .obtenerEstadoRegistro(email)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String estado = (String) response.body().get("estado");
                            if ("APROBADO".equals(estado)) {
                                SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
                                prefs.edit()
                                        .putString(ApiConfig.KEY_REGISTRATION_STATE, "APROBADO_PENDIENTE_PASS")
                                        .apply();

                                handler.removeCallbacks(pollingRunnable);
                                Intent intent = new Intent(RegistroEnProcesoActivity.this, RegistroPaso2Activity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        // Ignorar fallas temporales de red en el polling
                    }
                });
    }
}