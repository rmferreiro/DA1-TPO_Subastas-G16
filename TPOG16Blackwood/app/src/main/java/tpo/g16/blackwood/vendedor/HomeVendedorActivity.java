package tpo.g16.blackwood.vendedor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;

public class HomeVendedorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_vendedor);

        Button btnCrear = findViewById(R.id.btnCrearSolicitud);
        Button btnMisLotes = findViewById(R.id.btnMisLotes);
        Button btnMetricas = findViewById(R.id.btnMetricas);

        btnCrear.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearSolicitudActivity.class);
            startActivity(intent);
        });

        btnMisLotes.setOnClickListener(v -> {
            Intent intent = new Intent(this, MisLotesActivity.class);
            startActivity(intent);
        });

        btnMetricas.setOnClickListener(v -> {
            Intent intent = new Intent(this, MetricasVendedorActivity.class);
            startActivity(intent);
        });
    }
}