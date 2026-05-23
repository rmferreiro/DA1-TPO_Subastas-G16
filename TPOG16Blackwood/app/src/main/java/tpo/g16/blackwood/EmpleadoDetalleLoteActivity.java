package tpo.g16.blackwood;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoDetalleLoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_detalle_lote);

        // Recibir datos del lote
        String nombre = getIntent().getStringExtra("nombre");
        String duenio = getIntent().getStringExtra("duenio");
        String valor  = getIntent().getStringExtra("valor");

        ((TextView) findViewById(R.id.txt_nombre_lote)).setText(nombre);
        ((TextView) findViewById(R.id.txt_duenio_lote)).setText("Dueño: " + duenio);
        ((TextView) findViewById(R.id.txt_valor_lote)).setText("Valor estimado: USD " + valor);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Botón aceptar
        findViewById(R.id.btn_aceptar).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Aceptar lote")
                    .setMessage("¿Confirmás que aceptás el lote \"" + nombre + "\" para subasta?")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        ((TextView) findViewById(R.id.txt_estado)).setText("✓ ACEPTADO");
                        ((TextView) findViewById(R.id.txt_estado)).setTextColor(0xFF4CAF50);
                        findViewById(R.id.btn_aceptar).setEnabled(false);
                        findViewById(R.id.btn_rechazar).setEnabled(false);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Botón rechazar
        findViewById(R.id.btn_rechazar).setOnClickListener(v -> {
            EditText inputMotivo = new EditText(this);
            inputMotivo.setHint("Ingresá el motivo del rechazo");

            android.widget.FrameLayout container = new android.widget.FrameLayout(this);
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            );

            params.leftMargin = 50;
            params.rightMargin = 50;
            params.topMargin = 20;
            inputMotivo.setLayoutParams(params);
            container.addView(inputMotivo);

            new AlertDialog.Builder(this)
                    .setTitle("Rechazar lote")
                    .setMessage("Indicá el motivo del rechazo:")
                    .setView(container)
                    .setPositiveButton("Confirmar rechazo", (dialog, which) -> {
                        String motivo = inputMotivo.getText().toString();
                        if (!motivo.isEmpty()) {
                            ((TextView) findViewById(R.id.txt_estado)).setText("✗ RECHAZADO");
                            ((TextView) findViewById(R.id.txt_estado)).setTextColor(0xFFE53935);
                            ((TextView) findViewById(R.id.txt_motivo)).setText("Motivo: " + motivo);
                            ((TextView) findViewById(R.id.txt_motivo)).setVisibility(android.view.View.VISIBLE);
                            findViewById(R.id.btn_aceptar).setEnabled(false);
                            findViewById(R.id.btn_rechazar).setEnabled(false);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}
