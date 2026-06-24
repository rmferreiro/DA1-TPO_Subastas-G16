package tpo.g16.blackwood.subastas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.main.HomeActivity;

public class SubastaEnVivoActivity extends AppCompatActivity {

    private int precioActual = 15500;
    private int miPuja       = 16000;
    private static final int INCREMENTO = 500;
    private static final int PRECIO_MAX = 50000;

    private ProgressBar progressPuja;
    private TextView tvPrecio, tvMiPuja, tvOferta, tvBtnPujar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subasta_en_vivo);

        tvPrecio   = findViewById(R.id.tv_precio_actual);
        // tvMiPuja   = findViewById(R.id.tv_mi_puja);
        tvOferta   = findViewById(R.id.tv_oferta_input);
        tvBtnPujar = findViewById(R.id.tv_btn_pujar);
        progressPuja = findViewById(R.id.progress_puja);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Botón + (incrementar monto de puja)
        LinearLayout btnIncrementar = findViewById(R.id.btn_incrementar);
        if (btnIncrementar != null) {
            btnIncrementar.setOnClickListener(v -> {
                miPuja += INCREMENTO;
                actualizarUI();
            });
        }

        // Botón PUJAR
        LinearLayout btnPujar = findViewById(R.id.btn_pujar);
        if (btnPujar != null) {
            btnPujar.setOnClickListener(v -> {
                precioActual = miPuja;
                miPuja += INCREMENTO;
                actualizarUI();
            });
        }

        // Estado inicial
        actualizarUI();
        
        configurarBottomNav();
    }

    private void configurarBottomNav() {
        LinearLayout tabSubastas = findViewById(R.id.tab_subastas);
        LinearLayout tabPujas    = findViewById(R.id.tab_mis_pujas);

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

    private void actualizarUI() {
        String montoStr = "$ " + formatear(miPuja);
        String precioStr = "$ " + formatear(precioActual);

        if (tvPrecio   != null) tvPrecio.setText(precioStr + " USD");
        if (tvMiPuja   != null) tvMiPuja.setText(montoStr);
        if (tvOferta   != null) tvOferta.setText(montoStr);
        if (tvBtnPujar != null) tvBtnPujar.setText("Ofertar " + montoStr + " USD");

        // Progress: cuánto supera la puja al precio actual, sobre el rango 0-MAX
        if (progressPuja != null) {
            int rango = PRECIO_MAX - precioActual;
            int avance = miPuja - precioActual;
            int pct = (rango > 0) ? (int) (avance * 100f / rango) : 0;
            progressPuja.setProgress(Math.max(0, Math.min(100, pct)));
        }
    }

    private String formatear(int precio) {
        String s = String.valueOf(precio);
        StringBuilder sb = new StringBuilder();
        int start = s.length() % 3;
        if (start > 0) sb.append(s, 0, start);
        for (int i = start; i < s.length(); i += 3) {
            if (sb.length() > 0) sb.append('.');
            sb.append(s, i, i + 3);
        }
        return sb.toString();
    }
}
