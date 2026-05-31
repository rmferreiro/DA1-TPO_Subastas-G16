package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoMetricasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_metricas);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Ver logs de una subasta activa
        findViewById(R.id.card_subasta_1).setOnClickListener(v -> {
            Intent intent = new Intent(this, EmpleadoLogsPujasActivity.class);
            intent.putExtra("subasta", "Subasta 12 Marzo · 18:00");
            startActivity(intent);
        });

        findViewById(R.id.card_subasta_2).setOnClickListener(v -> {
            Intent intent = new Intent(this, EmpleadoLogsPujasActivity.class);
            intent.putExtra("subasta", "Subasta 15 Marzo · 20:00");
            startActivity(intent);
        });
    }
}