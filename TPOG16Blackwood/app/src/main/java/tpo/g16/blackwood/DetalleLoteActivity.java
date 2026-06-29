package tpo.g16.blackwood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
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
    private ViewPager2 vpImagenes;
    private LinearLayout llDots;
    private View cardObraArte;
    private TextView tvObraArtista, tvObraFecha, tvObraHistoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_lote);

        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        initViews();
        configurarListeners();

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
        vpImagenes = findViewById(R.id.vp_imagenes);
        llDots = findViewById(R.id.ll_dots);
        cardObraArte = findViewById(R.id.card_obra_arte);
        tvObraArtista = findViewById(R.id.tv_obra_artista);
        tvObraFecha = findViewById(R.id.tv_obra_fecha);
        tvObraHistoria = findViewById(R.id.tv_obra_historia);
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

                    Object subastaIdObj = body.get("subastaId");
                    Object productoIdObj = body.get("productoId");
                    if (subastaIdObj != null) {
                        subastaId = ((Number) subastaIdObj).intValue();
                    }
                    int productoId = -1;
                    if (productoIdObj != null) {
                        productoId = ((Number) productoIdObj).intValue();
                    }

                    // Issue 10: subtitulo muestra descripcionCompleta, no descripcionBreve
                    tvTitulo.setText((String) body.get("descripcionBreve"));
                    String descCompleta = (String) body.get("descripcionCompleta");
                    tvSubtitulo.setText(descCompleta != null && !descCompleta.isEmpty() ? descCompleta : (String) body.get("descripcionBreve"));

                    tvNPieza.setText((String) body.get("numeroPieza"));

                    Number pBase = (Number) body.get("precioBase");
                    Number pMinima = (Number) body.get("pujaMinima");

                    // Issue 11: usar moneda real de la subasta en lugar de hardcodear "USD"
                    String moneda = body.get("moneda") != null ? (String) body.get("moneda") : "ARS";
                    String simbolo = "USD".equalsIgnoreCase(moneda) ? "USD " : "$ ";
                    tvPrecioBase.setText(simbolo + pBase + " " + moneda);
                    tvPujaMinima.setText(simbolo + pMinima + " " + moneda);

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
                    } else {
                        if (tvEstadoTitulo != null) tvEstadoTitulo.setText("Disponible para pujar");
                        tvTiempoRestante.setText("En espera de puja...");
                        if (vEstadoStripCircle != null) vEstadoStripCircle.setBackgroundResource(R.drawable.circle_green);
                        if (vEstadoCardCircle != null) vEstadoCardCircle.setBackgroundResource(R.drawable.circle_green);
                    }

                    // Issues 8 y 9: cargar fotos y datos de obra de arte
                    if (productoId != -1) {
                        cargarProductoDetalle(productoId);
                    }
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

    @SuppressWarnings("unchecked")
    private void cargarProductoDetalle(int productoId) {
        RetrofitClient.getApiService().getProductoDetalle(productoId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> producto = response.body();

                    // Issue 8: galería de imágenes
                    Object fotosObj = producto.get("fotos");
                    if (fotosObj instanceof List) {
                        List<String> fotosBase64 = (List<String>) fotosObj;
                        if (!fotosBase64.isEmpty()) {
                            configurarGaleria(fotosBase64);
                        } else {
                            mostrarImagenPlaceholder();
                        }
                    } else {
                        mostrarImagenPlaceholder();
                    }

                    // Issue 9: datos de obra de arte
                    String tipo = (String) producto.get("tipo");
                    if ("OBRA_ARTE".equals(tipo) && cardObraArte != null) {
                        String artista = (String) producto.get("artista");
                        String fechaCreacion = (String) producto.get("fechaCreacion");
                        String historia = (String) producto.get("historia");

                        if (tvObraArtista != null)
                            tvObraArtista.setText(artista != null && !artista.isEmpty() ? artista : "-");
                        if (tvObraFecha != null)
                            tvObraFecha.setText(fechaCreacion != null && !fechaCreacion.isEmpty() ? fechaCreacion : "-");
                        if (tvObraHistoria != null)
                            tvObraHistoria.setText(historia != null && !historia.isEmpty() ? historia : "-");

                        cardObraArte.setVisibility(View.VISIBLE);
                    }
                } else {
                    mostrarImagenPlaceholder();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarImagenPlaceholder();
            }
        });
    }

    private void configurarGaleria(List<String> fotosBase64) {
        ImagePagerAdapter adapter = new ImagePagerAdapter(fotosBase64);
        vpImagenes.setAdapter(adapter);

        // Crear dots
        if (llDots != null) {
            llDots.removeAllViews();
            if (fotosBase64.size() > 1) {
                for (int i = 0; i < fotosBase64.size(); i++) {
                    View dot = new View(this);
                    int sizePx = dpToPx(8);
                    int marginPx = dpToPx(4);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
                    params.setMargins(marginPx, 0, marginPx, 0);
                    dot.setLayoutParams(params);
                    dot.setBackgroundResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
                    llDots.addView(dot);
                }

                vpImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        for (int i = 0; i < llDots.getChildCount(); i++) {
                            llDots.getChildAt(i).setBackgroundResource(
                                    i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
                        }
                    }
                });
            }
        }
    }

    private void mostrarImagenPlaceholder() {
        // Galería con imagen de placeholder cuando no hay fotos
        List<String> placeholder = new ArrayList<>();
        placeholder.add(null);
        configurarGaleria(placeholder);
        if (llDots != null) llDots.removeAllViews();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ── Adaptador de imágenes para ViewPager2 ────────────────────────────────

    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.VH> {

        private final List<String> fotosBase64;

        ImagePagerAdapter(List<String> fotosBase64) {
            this.fotosBase64 = fotosBase64;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new VH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String base64 = fotosBase64.get(position);
            if (base64 != null && !base64.isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bmp != null) {
                        holder.imageView.setImageBitmap(bmp);
                        return;
                    }
                } catch (Exception ignored) {}
            }
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        @Override
        public int getItemCount() {
            return fotosBase64.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView imageView;
            VH(ImageView iv) {
                super(iv);
                this.imageView = iv;
            }
        }
    }
}
