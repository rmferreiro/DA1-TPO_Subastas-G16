package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoRevisionLotesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_revision_lotes);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Click en cada lote para ver detalle
        findViewById(R.id.lote_1).setOnClickListener(v -> abrirDetalle("Reloj Patek Philippe 1950s", "Carlos Méndez", "15.000"));
        findViewById(R.id.lote_2).setOnClickListener(v -> abrirDetalle("Vajilla de plata Sterling", "Ana López", "8.000"));
        findViewById(R.id.lote_3).setOnClickListener(v -> abrirDetalle("Cuadro Berni - Escuela Rioplatense", "Roberto Silva", "22.000"));
        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }

    private void abrirDetalle(String nombre, String duenio, String valor) {
        Intent intent = new Intent(this, EmpleadoDetalleLoteActivity.class);
        intent.putExtra("nombre", nombre);
        intent.putExtra("duenio", duenio);
        intent.putExtra("valor", valor);
        startActivity(intent);
    }
}
