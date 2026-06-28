package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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

        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });

        findViewById(R.id.nav_mis_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga cada vez que volvés a esta pantalla, para que las métricas
        // siempre reflejen el estado actual (ej. si creaste o finalizaste una subasta).
        renderizarMetricas();
    }

    private void renderizarMetricas() {
        int subastasActivas = SubastaRepository.getInstance().contarEnSala();
        int ofertasEnVivo = MetricasRepository.getInstance().obtenerOfertasEnVivo();
        String estadoSistema = MetricasRepository.getInstance().obtenerEstadoSistema();

        ((TextView) findViewById(R.id.txt_subastas_activas)).setText(String.valueOf(subastasActivas));
        ((TextView) findViewById(R.id.txt_ofertas_en_vivo)).setText(String.valueOf(ofertasEnVivo));
        ((TextView) findViewById(R.id.txt_estado_sistema)).setText(estadoSistema);
    }
}
