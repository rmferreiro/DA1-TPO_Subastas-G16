package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NotificacionGanadorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacion_ganador);

        // Obtener datos del Intent
        Intent intent = getIntent();
        String productoDesc = intent.getStringExtra("productoDesc");
        String subastaDesc = intent.getStringExtra("subastaDesc");
        double precio = intent.getDoubleExtra("precio", 0.0);

        // Referencias a la UI
        android.widget.TextView tvLoteNombre = findViewById(R.id.tv_lote_nombre);
        android.widget.TextView tvLotePrecio = findViewById(R.id.tv_lote_precio);
        android.widget.TextView tvSubtotal = findViewById(R.id.tv_subtotal);
        android.widget.TextView tvComision = findViewById(R.id.tv_comision);
        android.widget.TextView tvTotal = findViewById(R.id.tv_total);
        android.widget.TextView tvEstadoSubtitulo = findViewById(R.id.tv_estado_subtitulo);

        // Calcular montos
        double comision = precio * 0.10;
        double envio = 120.0;
        double total = precio + comision + envio;

        // Setear textos
        if (tvLoteNombre != null && productoDesc != null) tvLoteNombre.setText(productoDesc);
        if (tvEstadoSubtitulo != null && subastaDesc != null) tvEstadoSubtitulo.setText("Lote adjudicado en " + subastaDesc);
        if (tvLotePrecio != null) tvLotePrecio.setText(String.format("$%,.0f", precio));
        if (tvSubtotal != null) tvSubtotal.setText(String.format("$%,.0f", precio));
        if (tvComision != null) tvComision.setText(String.format("$%,.0f", comision));
        if (tvTotal != null) tvTotal.setText(String.format("$%,.0f", total));

        // Confirmar → volver a lista
        findViewById(R.id.btn_confirmar).setOnClickListener(v -> irAInicio());
        configurarBottomNav();
    }

    private void irAInicio() {
        Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
        intent.putExtra("TAB_INDEX", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void configurarBottomNav() {
        android.widget.LinearLayout tabSubastas = findViewById(R.id.tab_subastas);
        android.widget.LinearLayout tabPujas    = findViewById(R.id.tab_mis_pujas);

        if (tabSubastas != null) {
            tabSubastas.setOnClickListener(v -> irAInicio());
        }
        if (tabPujas != null) {
            tabPujas.setOnClickListener(v -> {
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }
}
