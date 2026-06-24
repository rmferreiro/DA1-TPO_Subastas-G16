package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;

public class DetalleLoteActivity extends AppCompatActivity {

    private int itemId = -1;
    private int subastaId = -1;

    private TextView tvTitulo, tvSubtitulo, tvNPieza, tvPrecioBase, tvPujaMinima, tvDuenio, tvDuenioAvatar;
    private TextView tvTiempoRestante, tvDescripcion, tvCategoria, tvLoteNumero;
    private ImageView ivLote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_lote);

        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        initViews();
        configurarListeners();
        configurarBottomNav();

        if (itemId != -1) {
            cargarDetallesItem();
        } else {
            Toast.makeText(this, "Error: Item no especificado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvTitulo = findViewById(R.id.tv_titulo);
        tvSubtitulo = findViewById(R.id.tv_subtitulo);
        tvNPieza = findViewById(R.id.tv_n_pieza);
        tvPrecioBase = findViewById(R.id.tv_precio_base);
        tvPujaMinima = findViewById(R.id.tv_puja_minima);
        tvDuenio = findViewById(R.id.tv_duenio);
        tvDuenioAvatar = findViewById(R.id.tv_duenio_avatar);
        tvTiempoRestante = findViewById(R.id.tv_tiempo_restante);
        tvDescripcion = findViewById(R.id.tv_descripcion);
        tvCategoria = findViewById(R.id.tv_categoria);
        tvLoteNumero = findViewById(R.id.tv_lote_numero);
        ivLote = findViewById(R.id.iv_lote);
    }

    private void configurarListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_ir_sala).setOnClickListener(v -> {
            if (subastaId == -1) return;
            
            RetrofitClient.getApiService().unirseSubasta(subastaId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Intent intent = new Intent(DetalleLoteActivity.this, SubastaEnVivoActivity.class);
                            intent.putExtra("SUBASTA_ID", subastaId);
                            startActivity(intent);
                        } else {
                            try {
                                String errorStr = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                                Toast.makeText(DetalleLoteActivity.this, "No se pudo unir: " + errorStr, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(DetalleLoteActivity.this, "Error al unirse a la subasta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(DetalleLoteActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    private void cargarDetallesItem() {
        RetrofitClient.getApiService().getItemDetalle(itemId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    
                    subastaId = ((Number) body.get("subastaId")).intValue();
                    int productoId = ((Number) body.get("productoId")).intValue();
                    
                    tvTitulo.setText((String) body.get("descripcionBreve"));
                    tvSubtitulo.setText((String) body.get("descripcionBreve"));
                    tvNPieza.setText((String) body.get("numeroPieza"));
                    
                    Number pBase = (Number) body.get("precioBase");
                    Number pMinima = (Number) body.get("pujaMinima");
                    
                    tvPrecioBase.setText("$ " + pBase + " USD");
                    tvPujaMinima.setText("$ " + pMinima + " USD");
                    
                    String duenio = (String) body.get("duenioActual");
                    tvDuenio.setText(duenio);
                    if (duenio != null && duenio.length() >= 2) {
                        tvDuenioAvatar.setText(duenio.substring(0, 2).toUpperCase());
                    }
                    
                    tvDescripcion.setText((String) body.get("descripcionCompleta"));
                    tvCategoria.setText((String) body.get("categoria"));
                    tvLoteNumero.setText("LOTE #" + String.format("%03d", itemId));
                    
                    String subastado = (String) body.get("subastado");
                    TextView tvEstadoTitulo = findViewById(R.id.tv_estado_titulo);
                    View vEstadoStripCircle = findViewById(R.id.v_estado_strip_circle);
                    View vEstadoCardCircle = findViewById(R.id.v_estado_card_circle);
                    
                    if ("si".equalsIgnoreCase(subastado)) {
                        if (tvEstadoTitulo != null) tvEstadoTitulo.setText("Finalizado");
                        tvTiempoRestante.setText("Este ítem ya fue subastado.");
                        if (vEstadoStripCircle != null) vEstadoStripCircle.setBackgroundResource(R.drawable.circle_red);
                        if (vEstadoCardCircle != null) vEstadoCardCircle.setBackgroundResource(R.drawable.circle_red);
                        
                        // Opcional: Ocultar botón de ir a sala si ya se subastó, 
                        // pero dejémoslo por si la sala sigue activa con otros lotes.
                    } else {
                        if (tvEstadoTitulo != null) tvEstadoTitulo.setText("Disponible para pujar");
                        tvTiempoRestante.setText("En espera de puja...");
                        if (vEstadoStripCircle != null) vEstadoStripCircle.setBackgroundResource(R.drawable.circle_green);
                        if (vEstadoCardCircle != null) vEstadoCardCircle.setBackgroundResource(R.drawable.circle_green);
                    }

                    // Cargar imagen de la API
                    String imageUrl = RetrofitClient.BASE_URL + "api/productos/" + productoId + "/foto";
                    Glide.with(DetalleLoteActivity.this)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(ivLote);
                } else {
                    Toast.makeText(DetalleLoteActivity.this, "Error al cargar lote", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(DetalleLoteActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
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
