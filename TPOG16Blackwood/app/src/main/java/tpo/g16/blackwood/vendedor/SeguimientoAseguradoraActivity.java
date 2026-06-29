package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;

public class SeguimientoAseguradoraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_aseguradora);

        Button btnContactar = findViewById(R.id.btnContactarAseguradora);
        btnContactar.setOnClickListener(v ->
            Toast.makeText(this, "Contactando a la aseguradora...", Toast.LENGTH_SHORT).show()
        );
    }
}