package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoSubastasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_subastas);

        // Botón nueva subasta (sin pantalla por ahora)
        findViewById(R.id.btn_nueva_subasta).setOnClickListener(v -> {
            // TODO: EmpleadoCrearSubastaActivity
        });

        // Card "En sala" → subasta activa
        findViewById(R.id.card_subasta_en_sala).setOnClickListener(v -> {
            Intent intent = new Intent(this, EmpleadoSubastaActivaActivity.class);
            startActivity(intent);
        });
    }
}