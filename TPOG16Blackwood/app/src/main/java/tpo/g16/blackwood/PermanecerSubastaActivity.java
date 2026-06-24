package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class PermanecerSubastaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permanecer_subasta);

        // Obtener datos del Intent
        Intent intentData = getIntent();
        String productoDesc = intentData.getStringExtra("productoDesc");
        double importe = intentData.getDoubleExtra("importe", 0.0);
        int pujoId = intentData.getIntExtra("pujoId", -1);
        int subastaId = intentData.getIntExtra("SUBASTA_ID", -1);
        int itemId = intentData.getIntExtra("ITEM_ID", -1);

        android.view.View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        android.widget.TextView tvLoteNombrePrecio = findViewById(R.id.tv_lote_nombre_precio);
        android.widget.TextView tvPendientePago = findViewById(R.id.tv_pendiente_pago);

        String importeStr = "$" + importe;
        if (tvLoteNombrePrecio != null && productoDesc != null) {
            tvLoteNombrePrecio.setText(productoDesc + " · " + importeStr);
        }
        if (tvPendientePago != null) {
            tvPendientePago.setText(importeStr);
        }

        // Seguir en subasta → volver a subasta en vivo
        android.view.View btnPermanecer = findViewById(R.id.btn_permanecer);
        btnPermanecer.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubastaEnVivoActivity.class);
            intent.putExtra("SUBASTA_ID", subastaId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        if (subastaId != -1) {
            tpo.g16.blackwood.network.RetrofitClient.getApiService().getItemsDisponibles(subastaId).enqueue(new retrofit2.Callback<java.util.List<java.util.Map<String, Object>>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, retrofit2.Response<java.util.List<java.util.Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isEmpty()) {
                            btnPermanecer.setVisibility(android.view.View.GONE);
                        }
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, Throwable t) {}
            });
        }

        // Salir y pagar → pantalla Ganador
        findViewById(R.id.btn_salir).setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfirmarPagoActivity.class);
            if (pujoId != -1) {
                intent.putExtra("PUJA_ID", pujoId);
            }
            intent.putExtra("ITEM_ID", itemId);
            intent.putExtra("OFERTA", importe);
            intent.putExtra("DESCRIPCION", productoDesc);
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
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        if (tabPujas != null) {
            tabPujas.setOnClickListener(v -> {
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }
}
