package tpo.g16.blackwood.register;

import tpo.g16.blackwood.R;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroEnProcesoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_en_proceso);

        // Espera 3 segundos y navega
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(RegistroEnProcesoActivity.this, RegistroPaso2Activity.class);
            if (getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivity(intent);
            finish(); // evita volver atrás
        }, 3000);
    }
}