package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleSubastaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_subasta);

        int subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Botón ingresar → Subasta en vivo (Llamada API unirse)
        findViewById(R.id.btn_ingresar).setOnClickListener(v -> {
            if (subastaId == -1) return;
            
            tpo.g16.blackwood.network.RetrofitClient.getApiService().unirseSubasta(subastaId)
                .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Intent intent = new Intent(DetalleSubastaActivity.this, SubastaEnVivoActivity.class);
                            intent.putExtra("SUBASTA_ID", subastaId);
                            startActivity(intent);
                        } else {
                            try {
                                String errorStr = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                                android.widget.Toast.makeText(DetalleSubastaActivity.this, "No se pudo unir: " + errorStr, android.widget.Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                android.widget.Toast.makeText(DetalleSubastaActivity.this, "Error al unirse a la subasta", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                        android.widget.Toast.makeText(DetalleSubastaActivity.this, "Error de red: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
        });

        // Los listeners de las cards de lotes se configuran después de cargar el catálogo

        cargarItems(subastaId);

        // Bottom nav
        configurarBottomNav();
    }

    private void cargarItems(int subastaId) {
        if (subastaId == -1) return;
        tpo.g16.blackwood.network.RetrofitClient.getApiService().getCatalogo(subastaId).enqueue(new retrofit2.Callback<java.util.List<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, retrofit2.Response<java.util.List<java.util.Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    java.util.List<java.util.Map<String, Object>> items = response.body();
                    
                    if (items.size() > 0) {
                        int id1 = ((Number) items.get(0).get("itemId")).intValue();
                        findViewById(R.id.card_lote_1).setOnClickListener(v -> abrirDetalle(id1));
                    }
                    if (items.size() > 1) {
                        int id2 = ((Number) items.get(1).get("itemId")).intValue();
                        findViewById(R.id.card_lote_2).setOnClickListener(v -> abrirDetalle(id2));
                    }
                    if (items.size() > 2) {
                        int id3 = ((Number) items.get(2).get("itemId")).intValue();
                        findViewById(R.id.card_lote_3).setOnClickListener(v -> abrirDetalle(id3));
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, Throwable t) {
                // error
            }
        });
    }

    private void abrirDetalle(int itemId) {
        Intent intent = new Intent(DetalleSubastaActivity.this, DetalleLoteActivity.class);
        intent.putExtra("ITEM_ID", itemId);
        startActivity(intent);
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
        if (tabPerfil != null) {
            tabPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 2);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }
}
