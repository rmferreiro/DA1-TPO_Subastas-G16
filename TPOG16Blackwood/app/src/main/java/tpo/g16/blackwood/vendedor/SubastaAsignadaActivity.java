package tpo.g16.blackwood.vendedor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;

public class SubastaAsignadaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subasta_asignada);

        Button btnAceptar = findViewById(R.id.btnAceptarSubasta);
        Button btnRechazar = findViewById(R.id.btnRechazarSubasta);

        btnAceptar.setOnClickListener(v ->
            Toast.makeText(this, "Subasta aceptada", Toast.LENGTH_SHORT).show()
        );

        btnRechazar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RechazarPropuestaActivity.class);
            startActivity(intent);
        });
    }
}