package tpo.g16.blackwood;

import android.content.Intent;
import android.content.SharedPreferences;
import tpo.g16.blackwood.network.ApiConfig;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.SubastaResponse;

public class DetalleSubastaActivity extends AppCompatActivity {

    private int subastaId = -1;
    private String estadoSubasta = "";
    private String subastaMoneda = "ARS";
    private String subastaCategoria = "comun";
    private Long medioPagoIdSeleccionado = null;
    private boolean primeraVezEnResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_subasta);

        subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);


        // Deshabilitar el botón hasta saber el estado
        MaterialButton btnIngresar = findViewById(R.id.btn_ingresar);
        btnIngresar.setEnabled(false);
        btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
        btnIngresar.setTextColor(Color.parseColor("#757575"));

        // Botón ingresar → verifica requisitos y muestra modal si corresponde ANTES de abrir la sala
        btnIngresar.setOnClickListener(v -> {
            if (subastaId == -1) return;
            btnIngresar.setEnabled(false);

            SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
            String userCategory = prefs.getString(ApiConfig.KEY_USER_CATEGORIA, "comun");

            if (getCategoryRank(userCategory) < getCategoryRank(subastaCategoria)) {
                new AlertDialog.Builder(DetalleSubastaActivity.this)
                        .setTitle("Categoría insuficiente")
                        .setMessage("Tu categoría (" + userCategory.toUpperCase() + ") no permite acceder a esta subasta (requiere: " + subastaCategoria.toUpperCase() + ").")
                        .setPositiveButton("Aceptar", null)
                        .show();
                btnIngresar.setEnabled(true);
                return;
            }

            RetrofitClient.getApiService().unirseSubasta(subastaId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            verificarPagoYNavegar();
                        } else {
                            btnIngresar.setEnabled(true);
                            try {
                                String err = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                                Toast.makeText(DetalleSubastaActivity.this, "No se pudo unir: " + err, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(DetalleSubastaActivity.this, "Error al unirse a la subasta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnIngresar.setEnabled(true);
                        Toast.makeText(DetalleSubastaActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        cargarDetalle();
        cargarCatalogo();
        configurarBottomNav();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("SUBASTA_ID")) {
            subastaId = intent.getIntExtra("SUBASTA_ID", -1);
        }
        recargarContenido();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (primeraVezEnResume) {
            primeraVezEnResume = false;
            return;
        }
        recargarContenido();
    }

    private void recargarContenido() {
        if (subastaId == -1) return;
        cargarDetalle();
        cargarCatalogo();
    }

    /**
     * Llamado DESPUÉS de que unirseSubasta tiene éxito.
     * Consulta los medios de pago y, si el usuario no tiene uno válido,
     * muestra el diálogo de espectador ANTES de abrir la sala.
     */
    private void verificarPagoYNavegar() {
        RetrofitClient.getApiService().getMediosPago()
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call,
                                           Response<List<Map<String, Object>>> response) {
                        boolean tieneCompatible = false;
                        int countVerificadosActivos = 0;
                        medioPagoIdSeleccionado = null;

                        if (response.isSuccessful() && response.body() != null) {
                            for (Map<String, Object> mp : response.body()) {
                                if (Boolean.TRUE.equals(mp.get("verificado"))
                                        && Boolean.TRUE.equals(mp.get("activo"))) {
                                    countVerificadosActivos++;
                                    if (esMedioPagoCompatible(mp)) {
                                        tieneCompatible = true;
                                        Object idObj = mp.get("id");
                                        if (idObj instanceof Number) {
                                            medioPagoIdSeleccionado = ((Number) idObj).longValue();
                                        }
                                    }
                                }
                            }
                        }

                        if (tieneCompatible) {
                            navegarASubastaEnVivo(false);
                        } else {
                            if (countVerificadosActivos == 0) {
                                mostrarDialogoEspectador(
                                        "No tenés al menos un medio de pago verificado.\n\n¿Querés entrar como espectador?");
                            } else {
                                mostrarDialogoEspectador(
                                        "No tenés el medio de pago adecuado para esta subasta por la moneda.\n\n¿Querés entrar como espectador?");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        mostrarDialogoEspectador(
                                "No se pudo verificar tu medio de pago.\n\n¿Querés entrar como espectador?");
                    }
                });
    }

    private void mostrarDialogoEspectador(String mensaje) {
        new AlertDialog.Builder(DetalleSubastaActivity.this)
                .setTitle("Modo espectador")
                .setMessage(mensaje)
                .setPositiveButton("Entrar como espectador", (d, w) -> navegarASubastaEnVivo(true))
                .setNegativeButton("Cancelar", (d, w) -> cancelarIngreso())
                .setCancelable(false)
                .show();
    }

    private void navegarASubastaEnVivo(boolean comoEspectador) {
        Intent intent = new Intent(DetalleSubastaActivity.this, SubastaEnVivoActivity.class);
        intent.putExtra("SUBASTA_ID", subastaId);
        intent.putExtra("SPECTATOR_MODE", comoEspectador);
        if (!comoEspectador && medioPagoIdSeleccionado != null) {
            intent.putExtra("MEDIO_PAGO_ID", medioPagoIdSeleccionado);
        }
        startActivity(intent);
    }

    /**
     * Mismas reglas que el backend (MedioPagoStrategy.puedeOperarEnMoneda).
     */
    private boolean esMedioPagoCompatible(Map<String, Object> mp) {
        String tipo = (String) mp.get("tipo");
        String monedaMP = (String) mp.get("moneda");
        if (monedaMP == null) {
            monedaMP = "ARS";
        }

        if ("TARJETA_CREDITO".equals(tipo)) {
            return true;
        }
        if ("CUENTA_BANCARIA".equals(tipo)) {
            if ("USD".equalsIgnoreCase(subastaMoneda)) {
                return Boolean.TRUE.equals(mp.get("esInternacional")) || "USD".equalsIgnoreCase(monedaMP);
            }
            // Para subasta en ARS se requiere cuenta en ARS (CBU)
            return !Boolean.TRUE.equals(mp.get("esInternacional")) && "ARS".equalsIgnoreCase(monedaMP);
        }
        // Cheque certificado
        return subastaMoneda.equalsIgnoreCase(monedaMP);
    }

    private void cancelarIngreso() {
        // Liberar la sesión en el backend para no dejar al usuario "pegado"
        RetrofitClient.getApiService().salirSubasta(subastaId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {}
                    @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
        MaterialButton btnIngresar = findViewById(R.id.btn_ingresar);
        if (btnIngresar != null) btnIngresar.setEnabled(true);
    }

    private void cargarDetalle() {
        if (subastaId == -1) return;
        RetrofitClient.getApiService().getSubastaById(subastaId).enqueue(new Callback<SubastaResponse>() {
            @Override
            public void onResponse(Call<SubastaResponse> call, Response<SubastaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    poblarUI(response.body());
                }
            }
            @Override
            public void onFailure(Call<SubastaResponse> call, Throwable t) {}
        });
    }

    private void poblarUI(SubastaResponse s) {
        estadoSubasta = s.getEstado() != null ? s.getEstado().toUpperCase() : "";

        // --- Strip de estado ---
        View dotEstado = findViewById(R.id.dot_estado_detalle);
        TextView tvEstadoStrip = findViewById(R.id.tv_estado_strip);
        TextView tvCategoriaStrip = findViewById(R.id.tv_categoria_strip);

        String textoEstado;
        int colorEstado;
        if ("PENDIENTE".equals(estadoSubasta)) {
            textoEstado = "Pendiente";
            colorEstado = Color.parseColor("#1565C0"); // Azul
        } else if ("ACTIVA".equals(estadoSubasta)) {
            textoEstado = "Activa";
            colorEstado = Color.parseColor("#1B7A3E"); // Verde
        } else if ("FINALIZADA".equals(estadoSubasta)) {
            textoEstado = "Finalizada";
            colorEstado = Color.parseColor("#757575"); // Gris
        } else {
            textoEstado = estadoSubasta;
            colorEstado = Color.parseColor("#A7A9AC");
        }
        if (tvEstadoStrip != null) tvEstadoStrip.setText(textoEstado);
        if (dotEstado != null) dotEstado.setBackgroundTintList(ColorStateList.valueOf(colorEstado));

        // Categoría en negrita y color dorado/plateado según tipo
        if (tvCategoriaStrip != null) {
            String cat = s.getCategoria() != null ? s.getCategoria().toUpperCase() : "—";
            tvCategoriaStrip.setText(cat);
            switch (cat) {
                case "ORO":      tvCategoriaStrip.setTextColor(Color.parseColor("#C6A75E")); break;
                case "PLATINO":  tvCategoriaStrip.setTextColor(Color.parseColor("#808080")); break;
                case "PLATA":    tvCategoriaStrip.setTextColor(Color.parseColor("#A0A0A0")); break;
                case "ESPECIAL": tvCategoriaStrip.setTextColor(Color.parseColor("#4A90E2")); break;
                default:         tvCategoriaStrip.setTextColor(Color.parseColor("#6B6B6B")); break;
            }
        }

        // --- Fecha y hora ---
        TextView tvFechaHora = findViewById(R.id.tv_fecha_hora_detalle);
        if (tvFechaHora != null) {
            String fechaHora = tpo.g16.blackwood.common.TimeUtils.formatUtcToLocal(s.getFecha(), s.getHora());
            tvFechaHora.setText(fechaHora);
        }

        // --- Ubicación ---
        TextView tvUbicacion = findViewById(R.id.tv_ubicacion_detalle);
        if (tvUbicacion != null)
            tvUbicacion.setText("Ubicación: " + (s.getUbicacion() != null ? s.getUbicacion() : "—"));

        // --- Rematador ---
        TextView tvRematador = findViewById(R.id.tv_rematador_detalle);
        if (tvRematador != null)
            tvRematador.setText("Rematador: " + (s.getSubastadorNombre() != null ? s.getSubastadorNombre() : "—"));

        // --- Moneda ---
        TextView tvMoneda = findViewById(R.id.tv_moneda_detalle);
        if (tvMoneda != null) {
            subastaMoneda = s.getMoneda() != null ? s.getMoneda().toUpperCase() : "ARS";
            tvMoneda.setText("Moneda: " + subastaMoneda);
        }
        subastaCategoria = s.getCategoria() != null ? s.getCategoria().toLowerCase() : "comun";

        // --- Incremento mínimo según categoría (1% para normal, 20% para Oro y Platino) ---
        TextView tvIncremento = findViewById(R.id.tv_incremento);
        if (tvIncremento != null) {
            String cat = s.getCategoria() != null ? s.getCategoria().toUpperCase() : "";
            if ("ORO".equals(cat) || "PLATINO".equals(cat)) {
                tvIncremento.setText("Incremento mínimo: 20% del precio base");
            } else {
                tvIncremento.setText("Incremento mínimo: 1% del precio base");
            }
        }

        // --- Habilitar botón según estado ---
        MaterialButton btnIngresar = findViewById(R.id.btn_ingresar);
        if (btnIngresar != null) {
            if ("ACTIVA".equals(estadoSubasta)) {
                btnIngresar.setText("Ingresar a la subasta");
                btnIngresar.setEnabled(true);
                btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C0A062")));
                btnIngresar.setTextColor(Color.parseColor("#1C2A21"));
            } else if ("FINALIZADA".equals(estadoSubasta)) {
                btnIngresar.setText("Subasta finalizada");
                btnIngresar.setEnabled(false);
                btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                btnIngresar.setTextColor(Color.parseColor("#757575"));
            } else {
                btnIngresar.setText("Próximamente");
                btnIngresar.setEnabled(false);
                btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                btnIngresar.setTextColor(Color.parseColor("#757575"));
            }
        }
    }

    private void cargarCatalogo() {
        if (subastaId == -1) return;
        RetrofitClient.getApiService().getCatalogo(subastaId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    poblarLotes(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void poblarLotes(List<Map<String, Object>> items) {
        LinearLayout container = findViewById(R.id.layout_lotes_container);
        TextView tvNumLotes = findViewById(R.id.tv_num_lotes);
        if (container == null) return;

        // Actualizar cantidad de lotes
        if (tvNumLotes != null) {
            int n = items.size();
            tvNumLotes.setText(n + (n == 1 ? " lote" : " lotes"));
        }

        container.removeAllViews();

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            if (item == null) continue;

            // Obtener campos del item del catálogo
            // Obtener campos del item del catálogo
            int itemId = 0;
            if (item.get("itemId") != null) {
                try { itemId = ((Number) item.get("itemId")).intValue(); } catch (Exception ignored) {}
            }
            String descripcion = item.get("descripcion") != null ? (String) item.get("descripcion") : "Sin descripción";
            Object precioObj = item.get("precioBase");
            String precio = precioObj != null ? String.format("%.2f", ((Number) precioObj).doubleValue()) : "—";
            boolean subastado = "si".equalsIgnoreCase(String.valueOf(item.get("subastado")));
            Object mejorOfertaObj = item.get("mejorOferta");
            String nombreGanador = item.get("nombreGanador") != null ? (String) item.get("nombreGanador") : null;

            // Card del lote
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            card.setBackground(getResources().getDrawable(R.drawable.card_white, null));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(10));
            card.setLayoutParams(lp);
            card.setClickable(true);
            card.setFocusable(true);

            // Número de lote (itemId)
            TextView tvNumLote = new TextView(this);
            tvNumLote.setText(String.format("Lote #%03d", itemId));
            tvNumLote.setTextColor(Color.parseColor("#C6A75E"));
            tvNumLote.setTextSize(10f);
            card.addView(tvNumLote);

            // Nombre del lote
            TextView tvNombre = new TextView(this);
            tvNombre.setText(descripcion);
            tvNombre.setTextColor(Color.parseColor("#1A1A1A"));
            tvNombre.setTextSize(13f);
            LinearLayout.LayoutParams lpNombre = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpNombre.setMargins(0, dpToPx(3), 0, 0);
            tvNombre.setLayoutParams(lpNombre);
            card.addView(tvNombre);

            // Precio base
            TextView tvPrecio = new TextView(this);
            tvPrecio.setText("Precio base: " + precio);
            tvPrecio.setTextColor(Color.parseColor("#6B6B6B"));
            tvPrecio.setTextSize(11f);
            LinearLayout.LayoutParams lpPrecio = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpPrecio.setMargins(0, dpToPx(4), 0, 0);
            tvPrecio.setLayoutParams(lpPrecio);
            card.addView(tvPrecio);

            TextView tvEstadoLote = new TextView(this);
            if (subastado && mejorOfertaObj instanceof Number) {
                String oferta = String.format("%.2f", ((Number) mejorOfertaObj).doubleValue());
                String estadoLote = "Adjudicado: $" + oferta;
                if (nombreGanador != null && !nombreGanador.isBlank()) {
                    estadoLote += " · " + nombreGanador;
                }
                tvEstadoLote.setText(estadoLote);
                tvEstadoLote.setTextColor(Color.parseColor("#1B7A3E"));
            } else if (subastado) {
                tvEstadoLote.setText("Lote cerrado sin ofertas");
                tvEstadoLote.setTextColor(Color.parseColor("#757575"));
            } else if ("FINALIZADA".equals(estadoSubasta)) {
                tvEstadoLote.setText("Sin adjudicar");
                tvEstadoLote.setTextColor(Color.parseColor("#757575"));
            } else {
                tvEstadoLote.setText("Pendiente de subasta");
                tvEstadoLote.setTextColor(Color.parseColor("#1565C0"));
            }
            tvEstadoLote.setTextSize(11f);
            LinearLayout.LayoutParams lpEstado = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpEstado.setMargins(0, dpToPx(4), 0, 0);
            tvEstadoLote.setLayoutParams(lpEstado);
            card.addView(tvEstadoLote);

            // Click listener
            if (item.get("itemId") != null) {
                final int finalItemId = itemId;
                card.setOnClickListener(v -> abrirDetalle(finalItemId));
            }

            container.addView(card);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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

        if (labelSubastas != null) labelSubastas.setTextColor(Color.parseColor("#1C2A21"));
        if (labelMisPujas != null) labelMisPujas.setTextColor(Color.parseColor("#6B6B6B"));
        if (labelPerfil   != null) labelPerfil.setTextColor(Color.parseColor("#6B6B6B"));

        if (dotSubastas != null) dotSubastas.setVisibility(View.VISIBLE);
        if (dotMisPujas != null) dotMisPujas.setVisibility(View.INVISIBLE);
        if (dotPerfil   != null) dotPerfil.setVisibility(View.INVISIBLE);

        if (tabSubastas != null) tabSubastas.setOnClickListener(v -> {
            Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
            intent.putExtra("TAB_INDEX", 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        if (tabPujas != null) tabPujas.setOnClickListener(v -> {
            Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
            intent.putExtra("TAB_INDEX", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        if (tabPerfil != null) tabPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
            intent.putExtra("TAB_INDEX", 2);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private int getCategoryRank(String cat) {
        if (cat == null) return 0;
        switch (cat.toLowerCase()) {
            case "comun": return 1;
            case "especial": return 2;
            case "plata": return 3;
            case "oro": return 4;
            case "platino": return 5;
            default: return 0;
        }
    }
}
