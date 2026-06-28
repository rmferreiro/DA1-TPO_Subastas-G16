package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EmpleadoControlPujasActivity extends AppCompatActivity {

    private LinearLayout containerLogsPujas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_metricas_control_pujas);

        containerLogsPujas = findViewById(R.id.container_logs_pujas);

        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());
        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
        findViewById(R.id.nav_mis_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderizarLogs();
    }

    private void renderizarLogs() {
        containerLogsPujas.removeAllViews();
        List<LogPuja> logs = MetricasRepository.getInstance().obtenerLogsPujas();

        for (LogPuja log : logs) {
            TextView txt = new TextView(this);
            txt.setText(log.getTexto());
            txt.setTextColor(0xFF1A1A1A);
            txt.setTextSize(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 24; // ~10dp
            txt.setLayoutParams(params);
            containerLogsPujas.addView(txt);
        }
    }
}
