package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoPanelControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_metricas_panel_control);

        findViewById(R.id.card_subastas_vivo).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasVivoActivity.class));
        });

        findViewById(R.id.card_control_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoControlPujasActivity.class));
        });

        findViewById(R.id.card_logs).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoLogsPujasActivity.class));
        });

        findViewById(R.id.card_gestion_bienes).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });

        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
    }
}
