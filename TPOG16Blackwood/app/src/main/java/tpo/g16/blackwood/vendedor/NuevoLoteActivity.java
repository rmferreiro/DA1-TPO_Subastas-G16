package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;

public class NuevoLoteActivity extends AppCompatActivity {

    private Button btnAgregarElemento, btnEnviar;
    private EditText etDescripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_lote);

        // El botón de fotos ahora es un TextView decorativo, sin click
        btnAgregarElemento = findViewById(R.id.btnAgregarElemento);
        btnEnviar = findViewById(R.id.btnEnviarEvaluacion);
        etDescripcion = findViewById(R.id.etDescripcionLote);

        btnAgregarElemento.setOnClickListener(v ->
            Toast.makeText(this, "Agregar elemento próximamente", Toast.LENGTH_SHORT).show()
        );

        btnEnviar.setOnClickListener(v -> {
            String desc = etDescripcion.getText().toString().trim();
            if (desc.isEmpty()) {
                Toast.makeText(this, "Completá la descripción del lote", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Lote enviado a evaluación", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}