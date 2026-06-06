package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoSubastasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_gestion_subastas);

        // Botón nueva subasta
        findViewById(R.id.btn_nueva_subasta).setOnClickListener(v -> {
            // TODO: EmpleadoCrearSubastaActivity
        });

        // Botón filtros → abre bottom sheet
        findViewById(R.id.btn_filtros).setOnClickListener(v -> {
            FiltrosBottomSheet filtros = new FiltrosBottomSheet();
            filtros.show(getSupportFragmentManager(), "filtros");
        });

        // Card "En sala" → detalle de subasta
        findViewById(R.id.card_subasta_en_sala).setOnClickListener(v -> {
            Intent intent = new Intent(this, EmpleadoDetalleSubastaActivity.class);
            intent.putExtra(EmpleadoDetalleSubastaActivity.ESTADO, EmpleadoDetalleSubastaActivity.NO_INICIADA);
            startActivity(intent);
        });

        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }
}