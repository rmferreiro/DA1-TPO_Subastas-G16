package tpo.g16.blackwood.subastas;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.main.HomeActivity;

public class PermanecerSubastaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permanecer_subasta);

        // Seguir en subasta → volver a subasta en vivo
        findViewById(R.id.btn_permanecer).setOnClickListener(v -> {
            Intent intent = new Intent(this, SubastaEnVivoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Salir y pagar → pantalla Ganador
        findViewById(R.id.btn_salir).setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificacionGanadorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        configurarBottomNav();
    }

    private void configurarBottomNav() {
        android.widget.LinearLayout tabSubastas = findViewById(R.id.tab_subastas);
        android.widget.LinearLayout tabPujas    = findViewById(R.id.tab_mis_pujas);

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
