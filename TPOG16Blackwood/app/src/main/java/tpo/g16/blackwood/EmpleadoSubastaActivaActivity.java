package tpo.g16.blackwood;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoSubastaActivaActivity extends AppCompatActivity {

    private int segundos = 4 * 60 + 37; // 4:37 inicial
    private Handler handler = new Handler();
    private Runnable cronometro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_subasta_activa);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Arrancar cronómetro
        TextView txtTiempo = findViewById(R.id.txt_tiempo);
        cronometro = new Runnable() {
            @Override
            public void run() {
                if (segundos > 0) {
                    segundos--;
                    int min = segundos / 60;
                    int seg = segundos % 60;
                    txtTiempo.setText(String.format("%02d:%02d", min, seg));
                    handler.postDelayed(this, 1000);
                } else {
                    txtTiempo.setText("00:00");
                    txtTiempo.setTextColor(0xFFE53935);
                }
            }
        };
        handler.post(cronometro);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(cronometro);
    }
}