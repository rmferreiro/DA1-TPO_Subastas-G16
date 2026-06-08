package tpo.g16.blackwood.subastas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.main.HomeActivity;

public class DetalleSubastaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_subasta);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Botón ingresar → Subasta en vivo (stub por ahora)
        findViewById(R.id.btn_ingresar).setOnClickListener(v ->
                startActivity(new Intent(this, SubastaEnVivoActivity.class)));

        // Lote 1 → también va a Subasta en vivo
        findViewById(R.id.card_lote_1).setOnClickListener(v ->
                startActivity(new Intent(this, SubastaEnVivoActivity.class)));

        // Bottom nav
        configurarBottomNav();
    }

    private void configurarBottomNav() {
        LinearLayout tabSubastas = findViewById(R.id.tab_subastas);
        LinearLayout tabPujas    = findViewById(R.id.tab_mis_pujas);
        LinearLayout tabPerfil   = findViewById(R.id.tab_perfil);

        TextView labelSubastas = findViewById(R.id.tab_subastas_label);
        TextView labelMisPujas = findViewById(R.id.tab_pujas_label);
        TextView labelPerfil   = findViewById(R.id.tab_perfil_label);

        View dotSubastas = findViewById(R.id.tab_subastas_dot);
        View dotMisPujas = findViewById(R.id.tab_pujas_dot);
        View dotPerfil   = findViewById(R.id.tab_perfil_dot);

        if (labelSubastas != null) labelSubastas.setTextColor(android.graphics.Color.parseColor("#1C2A21"));
        if (labelMisPujas != null) labelMisPujas.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));
        if (labelPerfil != null) labelPerfil.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));

        if (dotSubastas != null) dotSubastas.setVisibility(View.VISIBLE);
        if (dotMisPujas != null) dotMisPujas.setVisibility(View.INVISIBLE);
        if (dotPerfil != null) dotPerfil.setVisibility(View.INVISIBLE);

        if (tabSubastas != null) {
            tabSubastas.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("TAB_INDEX", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        if (tabPujas != null) {
            tabPujas.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("TAB_INDEX", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }
}
