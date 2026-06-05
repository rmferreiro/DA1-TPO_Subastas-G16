package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NotificacionGanadorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacion_ganador);

        // Confirmar → volver a lista
        findViewById(R.id.btn_confirmar).setOnClickListener(v -> irAInicio());
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
