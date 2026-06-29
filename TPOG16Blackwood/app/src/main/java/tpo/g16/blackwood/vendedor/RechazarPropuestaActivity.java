package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;

public class RechazarPropuestaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rechazar_propuesta);

        EditText etMotivo = findViewById(R.id.etMotivoRechazo);
        Button btnConfirmar = findViewById(R.id.btnConfirmarRechazo);

        btnConfirmar.setOnClickListener(v -> {
            String motivo = etMotivo.getText().toString().trim();
            if (motivo.isEmpty()) {
                Toast.makeText(this, "Rechazo registrado. Se coordinará la devolución.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Motivo registrado. Se coordinará la devolución.", Toast.LENGTH_LONG).show();
            }
            finish();
        });
    }
}