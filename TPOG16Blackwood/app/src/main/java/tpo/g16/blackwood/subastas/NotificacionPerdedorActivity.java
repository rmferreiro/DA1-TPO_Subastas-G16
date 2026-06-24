package tpo.g16.blackwood.subastas;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.main.HomeActivity;

public class NotificacionPerdedorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacion_perdedor);

        // Ambos botones → volver a lista
        findViewById(R.id.btn_ver_subastas).setOnClickListener(v -> irAInicio());
        configurarBottomNav();
    }

    private void irAInicio() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("TAB_INDEX", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void configurarBottomNav() {
        android.widget.LinearLayout tabSubastas = findViewById(R.id.tab_subastas);
        android.widget.LinearLayout tabPujas    = findViewById(R.id.tab_mis_pujas);

        if (tabSubastas != null) {
            tabSubastas.setOnClickListener(v -> irAInicio());
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
