package tpo.g16.blackwood;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoLogsPujasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_logs_pujas);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Mostrar nombre de la subasta
        String subasta = getIntent().getStringExtra("subasta");
        if (subasta != null) {
            ((TextView) findViewById(R.id.txt_subasta_nombre)).setText(subasta);
        }
    }
}