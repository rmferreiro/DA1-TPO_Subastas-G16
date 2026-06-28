package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EmpleadoLogsPujasActivity extends AppCompatActivity {

    private LinearLayout containerLogsOfertas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_logs_ofertas);

        containerLogsOfertas = findViewById(R.id.container_logs_ofertas);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Mostrar nombre de la subasta
        String subasta = getIntent().getStringExtra("subasta");
        if (subasta != null) {
            ((TextView) findViewById(R.id.txt_subasta_nombre)).setText(subasta);
        }

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
        renderizarLogsOfertas();
    }

    private void renderizarLogsOfertas() {
        containerLogsOfertas.removeAllViews();
        List<LogOferta> logs = MetricasRepository.getInstance().obtenerLogsOfertas();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (LogOferta log : logs) {
            View fila = inflater.inflate(R.layout.item_log_oferta, containerLogsOfertas, false);

            TextView txtUsuario = fila.findViewById(R.id.txt_usuario);
            TextView txtMonto = fila.findViewById(R.id.txt_monto);
            TextView txtEstado = fila.findViewById(R.id.txt_estado);

            txtUsuario.setText(log.getUsuario());
            txtMonto.setText(log.getMonto());
            txtEstado.setText(log.getEstado());

            boolean esLider = LogOferta.LIDER.equals(log.getEstado());
            boolean esRechazada = LogOferta.RECHAZADA.equals(log.getEstado());

            if (esLider) {
                txtUsuario.setTextColor(0xFF1A1A1A);
                txtUsuario.setTypeface(null, android.graphics.Typeface.BOLD);
                txtMonto.setTextColor(0xFFC6A75E);
                txtMonto.setTypeface(null, android.graphics.Typeface.BOLD);
                txtEstado.setTextColor(0xFF4CAF50);
            } else if (esRechazada) {
                txtUsuario.setTextColor(0xFF555555);
                txtMonto.setTextColor(0xFF555555);
                txtEstado.setTextColor(0xFFE53935);
            } else {
                txtUsuario.setTextColor(0xFF555555);
                txtMonto.setTextColor(0xFF555555);
                txtEstado.setTextColor(0xFF888888);
            }

            containerLogsOfertas.addView(fila);
        }
    }
}
