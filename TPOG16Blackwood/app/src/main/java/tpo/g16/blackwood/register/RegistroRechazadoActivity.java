package tpo.g16.blackwood.register;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.login.LoginActivity;
import tpo.g16.blackwood.network.ApiConfig;

public class RegistroRechazadoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_rechazado);

        Button btnVolverInicio = findViewById(R.id.btn_volver_inicio);
        btnVolverInicio.setOnClickListener(v -> volverAlInicio());
    }

    private void volverAlInicio() {
        // 1. Limpiar SharedPreferences para olvidar el estado de registro actual
        SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ApiConfig.KEY_REGISTRATION_STATE);
        editor.remove(ApiConfig.KEY_REGISTRATION_EMAIL);
        editor.remove(ApiConfig.KEY_REGISTRATION_NOMBRE);
        editor.remove(ApiConfig.KEY_REGISTRATION_APELLIDO);
        editor.apply();

        // 2. Redirigir a LoginActivity y limpiar la pila de navegación
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
