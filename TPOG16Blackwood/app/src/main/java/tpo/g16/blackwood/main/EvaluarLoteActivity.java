package tpo.g16.blackwood.main;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.R;
import tpo.g16.blackwood.network.RetrofitClient;

public class EvaluarLoteActivity extends AppCompatActivity {

    private TextView txtDuenio, txtTitulo, txtSubtitulo, txtDescripcion;
    private HorizontalScrollView scrollFotos;
    private LinearLayout layoutFotos;
    private LinearLayout containerObra;
    private TextView txtArtista, txtFecha, txtHistoria;

    private TextView btnDecideAceptar, btnDecideRechazar;
    private LinearLayout containerAceptar, containerRechazar;
    
    private TextView btnEvalMonedaArs, btnEvalMonedaUsd;
    private EditText etDecidePrecio, etDecideMotivos;
    private MaterialButton btnEnviar;

    private int productoId;
    private String decisionSeleccionada = "ACEPTADO";
    private String monedaSeleccionada = "ARS";
    private double precioOriginal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluar_lote);

        // Configurar Header
        TextView headerSubtitle = findViewById(R.id.header_subtitle);
        if (headerSubtitle != null) {
            headerSubtitle.setText("Evaluación de lote");
        }

        productoId = getIntent().getIntExtra("productoId", 0);

        // Vincular componentes
        txtDuenio = findViewById(R.id.txt_eval_duenio);
        txtTitulo = findViewById(R.id.txt_eval_titulo);
        txtSubtitulo = findViewById(R.id.txt_eval_subtitulo);
        txtDescripcion = findViewById(R.id.txt_eval_descripcion);
        
        scrollFotos = findViewById(R.id.scroll_eval_fotos);
        layoutFotos = findViewById(R.id.layout_eval_fotos);

        containerObra = findViewById(R.id.container_eval_obra);
        txtArtista = findViewById(R.id.txt_eval_artista);
        txtFecha = findViewById(R.id.txt_eval_fecha);
        txtHistoria = findViewById(R.id.txt_eval_historia);

        btnDecideAceptar = findViewById(R.id.btn_decide_aceptar);
        btnDecideRechazar = findViewById(R.id.btn_decide_rechazar);
        containerAceptar = findViewById(R.id.container_decide_aceptar);
        containerRechazar = findViewById(R.id.container_decide_rechazar);
        
        btnEvalMonedaArs = findViewById(R.id.btn_eval_moneda_ars);
        btnEvalMonedaUsd = findViewById(R.id.btn_eval_moneda_usd);
        
        etDecidePrecio = findViewById(R.id.et_decide_precio);
        etDecideMotivos = findViewById(R.id.et_decide_motivos);
        btnEnviar = findViewById(R.id.btn_enviar_decision);

        // Configurar selector de decisión (Aceptar / Rechazar)
        btnDecideAceptar.setOnClickListener(v -> alternarDecision("ACEPTADO"));
        btnDecideRechazar.setOnClickListener(v -> alternarDecision("RECHAZADO"));
        alternarDecision("ACEPTADO"); // Default

        // Configurar selector de moneda
        btnEvalMonedaArs.setOnClickListener(v -> seleccionarMoneda("ARS"));
        btnEvalMonedaUsd.setOnClickListener(v -> seleccionarMoneda("USD"));
        seleccionarMoneda("ARS"); // Default

        btnEnviar.setOnClickListener(v -> enviarDecision());

        obtenerDetalles();
    }

    private void alternarDecision(String decision) {
        decisionSeleccionada = decision;
        if ("ACEPTADO".equals(decision)) {
            btnDecideAceptar.setBackgroundResource(R.drawable.segment_active_bg);
            btnDecideAceptar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnDecideAceptar.setTextColor(Color.WHITE);

            btnDecideRechazar.setBackgroundColor(Color.TRANSPARENT);
            btnDecideRechazar.setTextColor(Color.parseColor("#1C2A21"));

            containerAceptar.setVisibility(View.VISIBLE);
            containerRechazar.setVisibility(View.GONE);
        } else {
            btnDecideRechazar.setBackgroundResource(R.drawable.segment_active_bg);
            btnDecideRechazar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            btnDecideRechazar.setTextColor(Color.WHITE);

            btnDecideAceptar.setBackgroundColor(Color.TRANSPARENT);
            btnDecideAceptar.setTextColor(Color.parseColor("#1C2A21"));

            containerRechazar.setVisibility(View.VISIBLE);
            containerAceptar.setVisibility(View.GONE);
        }
    }

    private void seleccionarMoneda(String moneda) {
        monedaSeleccionada = moneda;
        if ("ARS".equals(moneda)) {
            btnEvalMonedaArs.setBackgroundResource(R.drawable.segment_active_bg);
            btnEvalMonedaArs.setTextColor(Color.WHITE);
            btnEvalMonedaUsd.setBackgroundColor(Color.TRANSPARENT);
            btnEvalMonedaUsd.setTextColor(Color.parseColor("#1C2A21"));
        } else {
            btnEvalMonedaUsd.setBackgroundResource(R.drawable.segment_active_bg);
            btnEvalMonedaUsd.setTextColor(Color.WHITE);
            btnEvalMonedaArs.setBackgroundColor(Color.TRANSPARENT);
            btnEvalMonedaArs.setTextColor(Color.parseColor("#1C2A21"));
        }
    }

    private void obtenerDetalles() {
        RetrofitClient.getApiService().getProductoDetalle(productoId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> data = response.body();

                            String desc = (String) data.get("descripcion");
                            String sub = (String) data.get("subtitulo");
                            String duenioNombre = (String) data.get("duenioNombre");
                            String tipo = (String) data.get("tipo");
                            
                            // Para precio sugerido original
                            Double precioSugerido = (Double) data.get("precioEstimado");
                            precioOriginal = precioSugerido != null ? precioSugerido : 0.0;
                            String moneda = (String) data.get("moneda");
                            String monedaOriginal = moneda != null ? moneda : "ARS";

                            String descCompleta = (String) data.get("descripcionCompleta");

                            txtTitulo.setText(desc != null && !desc.trim().isEmpty() ? desc : "Lote sin título");
                            txtSubtitulo.setText(sub != null && !sub.trim().isEmpty() ? sub : "Sin subtítulo");
                            txtDescripcion.setText("Descripción del lote:\n" + (descCompleta != null && !descCompleta.trim().isEmpty() ? descCompleta : "Sin descripción"));
                            txtDuenio.setText("Dueño: " + (duenioNombre != null ? duenioNombre : "Desconocido"));

                            etDecidePrecio.setText(String.valueOf(precioOriginal));
                            seleccionarMoneda(monedaOriginal);

                            // Renderizar fotos del lote
                            if (data.containsKey("fotos")) {
                                List<String> fotosList = (List<String>) data.get("fotos");
                                if (fotosList != null && !fotosList.isEmpty()) {
                                    scrollFotos.setVisibility(View.VISIBLE);
                                    layoutFotos.removeAllViews();
                                    for (String base64 : fotosList) {
                                        try {
                                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                            if (bitmap != null) {
                                                ImageView imageView = new ImageView(EvaluarLoteActivity.this);
                                                int sizeInPx = (int) (110 * getResources().getDisplayMetrics().density);
                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                                                params.setMargins(0, 0, 16, 0);
                                                imageView.setLayoutParams(params);
                                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                imageView.setImageBitmap(bitmap);
                                                layoutFotos.addView(imageView);
                                            }
                                        } catch (Exception e) {
                                            // Ignorar fotos corruptas
                                        }
                                    }
                                }
                            }

                            if ("OBRA_ARTE".equals(tipo)) {
                                containerObra.setVisibility(View.VISIBLE);
                                String artista = (String) data.get("artista");
                                String fecha = (String) data.get("fechaCreacion");
                                String historia = (String) data.get("historia");

                                txtArtista.setText("Artista: " + (artista != null ? artista : "Desconocido"));
                                txtFecha.setText("Creación: " + (fecha != null ? fecha : "Sin fecha"));
                                txtHistoria.setText("Historia: " + (historia != null ? historia : "-"));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(EvaluarLoteActivity.this, "Error al conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void enviarDecision() {
        btnEnviar.setEnabled(false);

        Map<String, Object> request = new HashMap<>();
        request.put("decision", decisionSeleccionada);

        if ("ACEPTADO".equals(decisionSeleccionada)) {
            String precioStr = etDecidePrecio.getText().toString().trim();
            if (precioStr.isEmpty()) {
                etDecidePrecio.setError("Debe indicar el precio base");
                btnEnviar.setEnabled(true);
                return;
            }
            request.put("precioBase", Double.parseDouble(precioStr));
            request.put("comision", 0.00); // comision por defecto requerida por backend
            request.put("moneda", monedaSeleccionada); // Moneda seleccionada por el Admin
        } else {
            String motivos = etDecideMotivos.getText().toString().trim();
            if (motivos.isEmpty()) {
                etDecideMotivos.setError("Debe explicar el motivo de rechazo");
                btnEnviar.setEnabled(true);
                return;
            }
            request.put("motivo", motivos);
        }

        RetrofitClient.getApiService().revisarProducto(productoId, request)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            if ("ACEPTADO".equals(decisionSeleccionada)) {
                                Toast.makeText(EvaluarLoteActivity.this, "Propuesta enviada", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EvaluarLoteActivity.this, "Rechazo enviado", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        } else {
                            btnEnviar.setEnabled(true);
                            Toast.makeText(EvaluarLoteActivity.this, "Error al enviar decisión", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnEnviar.setEnabled(true);
                        Toast.makeText(EvaluarLoteActivity.this, "Falla al conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
