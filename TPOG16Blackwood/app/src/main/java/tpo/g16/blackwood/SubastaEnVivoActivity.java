package tpo.g16.blackwood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.ApiConfig;
import tpo.g16.blackwood.network.AuctionStompClient;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.SubastaResponse;
import tpo.g16.blackwood.network.models.websocket.AuctionFinishedUpdate;
import tpo.g16.blackwood.network.models.websocket.AuctionStateUpdate;
import tpo.g16.blackwood.network.models.websocket.AuctionUsersUpdate;
import tpo.g16.blackwood.network.models.websocket.BidError;
import tpo.g16.blackwood.network.models.websocket.LotChangeUpdate;

public class SubastaEnVivoActivity extends AppCompatActivity
        implements AuctionStompClient.AuctionEventListener {

    private static final String TAG = "SubastaEnVivoActivity";

    // ── IDs de la sesión actual ──────────────────────────────────────────────
    private int subastaId;
    private int currentItemId = -1;
    private Long miMedioPagoId = null;

    // ── Estado de precios (double para precisión con decimales) ──────────────
    private double precioActual = 0.0;
    private double miPuja = 0.0;
    private double precioBase = 1000.0;

    // ── Datos de la subasta ──────────────────────────────────────────────────
    private boolean isOroOrPlatino = false;
    private boolean sinLimiteMaximo = false;
    private String subastaMoneda = "ARS";
    private String subastaCategoria = "comun";
    private String currentUsername = "";
    private boolean isSpectator = false;
    private long clientServerOffset = 0;

    // ── Límites de la siguiente puja (provistos por el servidor) ────────────
    private double siguientePujaMinima = 0.0;
    private double siguientePujaMaxima = Double.MAX_VALUE;

    // ── Estado de puja en proceso ─────────────────────────────────────────
    private boolean pujaEnProceso = false;
    private final Handler bidTimeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable bidTimeoutRunnable;
    private static final long BID_TIMEOUT_MS = 15_000L;

    // ── Nombre del usuario (para detectar si ganó un lote) ───────────────
    private String currentUserNombre = "";

    // ── Vistas ───────────────────────────────────────────────────────────────
    private ImageView ivLoteImagen;
    private ProgressBar progressPuja;
    private ProgressBar progressBid;
    private TextView tvPrecio, tvBtnPujar, tvTiempo, tvRealizarPujaLabel, tvLoteBadge;
    private EditText etOfertaInput;
    private TextView tvLoteTitulo, tvLoteDescripcion;
    private TextView tvAvatarIniciales, tvNombrePostor, tvParticipantesCount;
    private TextView tvTituloPuja, tvBadgeLider;
    private LinearLayout layoutAvatars;
    private Long limiteFinalizacionEpoch = null;

    // ── Sección obra de arte ─────────────────────────────────────────────
    private View cardVivoObraArte;
    private TextView tvVivoObraArtista, tvVivoObraHistoria;

    // ── WebSocket ────────────────────────────────────────────────────────────
    private AuctionStompClient auctionStompClient;
    private CountDownTimer countDownTimer;
    private static final long TIMER_TOTAL_DURATION_MS = 300_000L; // 5 minutos

    // ────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subasta_en_vivo);

        subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);
        if (subastaId == -1) {
            Toast.makeText(this, "Error: Subasta no especificada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Si DetalleSubastaActivity ya preguntó y el usuario eligió espectador,
        // activar el modo antes de que la pantalla muestre cualquier cosa.
        if (getIntent().getBooleanExtra("SPECTATOR_MODE", false)) {
            isSpectator = true;
        }

        SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
        currentUsername = prefs.getString(ApiConfig.KEY_USER_EMAIL, "");
        currentUserNombre = prefs.getString(ApiConfig.KEY_USER_NOMBRE, "");
        String token = prefs.getString(ApiConfig.KEY_ACCESS_TOKEN, "");

        RetrofitClient.getInstance(this);

        bindViews();

        // Un único call que trae TODO el estado inicial de la subasta
        fetchEstadoVivo(token);
    }

    private void bindViews() {
        tvPrecio           = findViewById(R.id.tv_precio_actual);
        etOfertaInput      = findViewById(R.id.et_oferta_input);
        tvRealizarPujaLabel = findViewById(R.id.tv_realizar_puja_label);
        tvLoteBadge        = findViewById(R.id.tv_lote_badge);
        tvBtnPujar         = findViewById(R.id.tv_btn_pujar);
        progressPuja       = findViewById(R.id.progress_puja);
        progressBid        = findViewById(R.id.progress_bid);
        tvTiempo           = findViewById(R.id.tv_tiempo);
        ivLoteImagen       = findViewById(R.id.iv_lote_imagen);
        tvLoteTitulo       = findViewById(R.id.tv_lote_titulo);
        tvLoteDescripcion  = findViewById(R.id.tv_lote_descripcion);
        tvAvatarIniciales  = findViewById(R.id.tv_avatar_iniciales);
        tvNombrePostor     = findViewById(R.id.tv_nombre_postor);
        tvParticipantesCount = findViewById(R.id.tv_participantes_count);
        tvTituloPuja       = findViewById(R.id.tv_titulo_puja);
        tvBadgeLider       = findViewById(R.id.tv_badge_lider);
        layoutAvatars      = findViewById(R.id.layout_avatars);

        // Ocultar número de lote inferior duplicado
        TextView tvLoteNumero = findViewById(R.id.tv_lote_numero);
        if (tvLoteNumero != null) tvLoteNumero.setVisibility(android.view.View.GONE);

        // Obra de arte
        cardVivoObraArte = findViewById(R.id.card_vivo_obra_arte);
        tvVivoObraArtista = findViewById(R.id.tv_vivo_obra_artista);
        tvVivoObraHistoria = findViewById(R.id.tv_vivo_obra_historia);

        if (tvParticipantesCount != null) tvParticipantesCount.setText("1 en línea");
        if (layoutAvatars != null) layoutAvatars.removeAllViews();

        // ── Botones +/- y Pujar ──────────────────────────────────────────────
        LinearLayout btnDecrementar = findViewById(R.id.btn_decrementar);
        if (btnDecrementar != null) {
            btnDecrementar.setOnClickListener(v -> {
                if (isSpectator) return;
                double inputVal = parseInput();
                double step = calcularStep();
                double nextVal = inputVal - step;
                // El mínimo posible es siguientePujaMinima
                if (nextVal >= siguientePujaMinima) {
                    miPuja = nextVal;
                    actualizarUITextos();
                }
            });
        }

        LinearLayout btnIncrementar = findViewById(R.id.btn_incrementar);
        if (btnIncrementar != null) {
            btnIncrementar.setOnClickListener(v -> {
                if (isSpectator) return;
                double nextVal = parseInput() + calcularStep();
                // Para oro/platino no hay tope; para el resto respetar el máximo del enunciado
                miPuja = (sinLimiteMaximo || nextVal <= siguientePujaMaxima)
                        ? nextVal : siguientePujaMaxima;
                actualizarUITextos();
            });
        }

        LinearLayout btnPujar = findViewById(R.id.btn_pujar);
        if (btnPujar != null) {
            btnPujar.setOnClickListener(v -> {
                if (isSpectator) {
                    Toast.makeText(this, "Estás en modo visitante, no puedes ofertar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                double inputVal = parseInput();
                if (inputVal < siguientePujaMinima) {
                    Toast.makeText(this,
                            "La puja mínima es " + formatMoneda(siguientePujaMinima),
                            Toast.LENGTH_SHORT).show();
                } else if (!sinLimiteMaximo && inputVal > siguientePujaMaxima) {
                    Toast.makeText(this,
                            "La puja máxima es " + formatMoneda(siguientePujaMaxima),
                            Toast.LENGTH_SHORT).show();
                } else {
                    miPuja = inputVal;
                    realizarPuja();
                }
            });
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Carga inicial de estado via REST
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Llama a GET /api/subastas/{id}/estado-vivo que devuelve el estado completo:
     * item actual, mejor oferta, precio base, límites de la próxima puja y tiempo restante.
     * Esto reemplaza los dos llamados separados que existían antes.
     */
    private void fetchEstadoVivo(String token) {
        RetrofitClient.getApiService().getEstadoVivo(subastaId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        sincronizarReloj(response);
                        if (response.isSuccessful() && response.body() != null) {
                            procesarEstadoVivo(response.body(), token);
                        } else {
                            Log.e(TAG, "Error en getEstadoVivo: " + response.code());
                            Toast.makeText(SubastaEnVivoActivity.this,
                                    "Error al obtener estado de la subasta", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "Fallo de red en getEstadoVivo", t);
                        Toast.makeText(SubastaEnVivoActivity.this,
                                "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void procesarEstadoVivo(Map<String, Object> estado, String token) {
        // ── Datos de la subasta ──────────────────────────────────────────────
        subastaMoneda   = getStr(estado, "moneda", "ARS");
        subastaCategoria = getStr(estado, "categoria", "comun");
        isOroOrPlatino  = subastaCategoria.equalsIgnoreCase("oro")
                || subastaCategoria.equalsIgnoreCase("platino");

        String estadoStr = getStr(estado, "estadoSubasta", "");
        if ("FINALIZADA".equalsIgnoreCase(estadoStr)) {
            Toast.makeText(this, "La subasta ha finalizado", Toast.LENGTH_LONG).show();
            irAHome(0);
            return;
        }

        Object limiteObj = estado.get("limiteFinalizacionEpoch");
        if (limiteObj != null) {
            limiteFinalizacionEpoch = ((Number) limiteObj).longValue();
        }

        // ── Header de categoría ──────────────────────────────────────────────
        TextView tvHeaderCalidad = findViewById(R.id.tv_header_calidad);
        if (tvHeaderCalidad != null) {
            tvHeaderCalidad.setText(subastaCategoria.toUpperCase());
        }

        // ── Item activo ──────────────────────────────────────────────────────
        Map<String, Object> itemActual = null;
        Object itemObj = estado.get("itemActual");
        if (itemObj instanceof Map) {
            itemActual = (Map<String, Object>) itemObj;
        }

        if (itemActual == null) {
            Toast.makeText(this, "No hay lotes activos en esta subasta", Toast.LENGTH_LONG).show();
            irAHome(0);
            return;
        }

        currentItemId = getInt(itemActual, "itemId", -1);
        int productoId = getInt(itemActual, "productoId", -1);
        // Número de orden para mostrar en UI ("Lote #1", "Lote #2"…). Fallback al itemId si no está disponible.
        int lotOrden = getInt(itemActual, "orden", currentItemId);

        Object precioBaseObj = itemActual.get("precioBase");
        if (!(precioBaseObj instanceof Number)) {
            Toast.makeText(this, "No pudimos unirte a la subasta, intenta más tarde", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        precioBase = ((Number) precioBaseObj).doubleValue();

        // Mejor oferta actual (puede ser null si nadie pujó todavía)
        Object mejorOfertaObj = itemActual.get("mejorOferta");
        if (mejorOfertaObj != null) {
            precioActual = ((Number) mejorOfertaObj).doubleValue();
        } else {
            precioActual = precioBase; // Precio base como punto de partida
        }

        // Límites provistos por el servidor (fuente de verdad)
        sinLimiteMaximo = getBool(itemActual, "sinLimiteMaximo", isOroOrPlatino);
        siguientePujaMinima = getDouble(itemActual, "siguientePujaMinima",
                isOroOrPlatino ? precioActual + 1.0 : precioActual + precioBase * 0.01);
        Object maxObj = itemActual.get("siguientePujaMaxima");
        siguientePujaMaxima = (maxObj != null) ? ((Number) maxObj).doubleValue() : Double.MAX_VALUE;

        miPuja = siguientePujaMinima;

        // Descripción del lote
        String desc = getStr(itemActual, "descripcionCompleta",
                getStr(itemActual, "descripcion", "Lote sin título"));
        String descCatalogo = getStr(itemActual, "descripcion", "");

        if (tvLoteBadge != null) tvLoteBadge.setText(String.format("LOTE #%03d", lotOrden));
        if (tvLoteTitulo != null) tvLoteTitulo.setText(desc);
        if (tvLoteDescripcion != null) tvLoteDescripcion.setText(descCatalogo);

        // Cargar imagen — usar signature por itemId para invalidar cache Glide entre lotes
        if (ivLoteImagen != null && productoId != -1) {
            String imageUrl = RetrofitClient.BASE_URL + "api/productos/" + productoId + "/foto";
            Glide.with(this).load(imageUrl)
                    .signature(new ObjectKey("item-" + currentItemId))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivLoteImagen);
        }

        // Issue 9: cargar datos de obra de arte para el lote inicial
        if (productoId != -1) {
            cargarInfoObraArte(productoId);
        }

        // Postor actual
        String nombrePostor = getStr(itemActual, "nombreMejorPostor", "Blackwood Subastas");
        int numPostor = mejorOfertaObj != null ? 1 : 999;
        actualizarUIConPostor(nombrePostor, numPostor);
        actualizarUITextos();

        // ── Verificar requisitos antes de conectar WebSocket ─────────────────
        fetchMedioPagoYConectar(token);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Verificación de medio de pago + requisitos de acceso
    // ────────────────────────────────────────────────────────────────────────

    private void fetchMedioPagoYConectar(String token) {
        RetrofitClient.getApiService().getMediosPago()
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call,
                                           Response<List<Map<String, Object>>> response) {
                        sincronizarReloj(response);
                        if (response.isSuccessful() && response.body() != null) {
                            for (Map<String, Object> mp : response.body()) {
                                if (!Boolean.TRUE.equals(mp.get("verificado"))
                                        || !Boolean.TRUE.equals(mp.get("activo"))) continue;

                                // Issue 19: filtrar por compatibilidad de moneda
                                if (!esMedioPagoCompatible(mp)) continue;

                                miMedioPagoId = ((Number) mp.get("id")).longValue();
                                break;
                            }
                        }
                        verificarRequisitosYConectar(token);
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        verificarRequisitosYConectar(token);
                    }
                });
    }

    /**
     * Determina si un medio de pago es compatible con la moneda de esta subasta.
     * - TARJETA_CREDITO: siempre compatible (ARS y USD).
     * - CUENTA_BANCARIA y CHEQUE_CERTIFICADO: solo si su moneda coincide con la subasta.
     */
    private boolean esMedioPagoCompatible(Map<String, Object> mp) {
        String tipo = (String) mp.get("tipo");
        if ("TARJETA_CREDITO".equals(tipo)) return true;
        String monedaMP = (String) mp.get("moneda");
        return subastaMoneda.equalsIgnoreCase(monedaMP != null ? monedaMP : "ARS");
    }

    private void verificarRequisitosYConectar(String token) {
        // Si el usuario ya eligió modo espectador en DetalleSubastaActivity, entrar directo.
        if (isSpectator) {
            setSpectatorMode();
            iniciarTimerYConectar(token);
            return;
        }

        SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
        String userCategory = prefs.getString(ApiConfig.KEY_USER_CATEGORIA, "comun");

        boolean cumplenNivel = getCategoryRank(userCategory) >= getCategoryRank(subastaCategoria);
        boolean cumplenPago  = miMedioPagoId != null;

        if (!cumplenNivel || !cumplenPago) {
            // Solo como resguardo: si el modal de DetalleSubastaActivity falló por alguna razón,
            // mostrarlo aquí. En flujo normal esto nunca debería ejecutarse.
            StringBuilder razon = new StringBuilder();
            if (!cumplenNivel) razon.append("el nivel requerido (").append(subastaCategoria).append(")");
            if (!cumplenPago) {
                if (razon.length() > 0) razon.append(" ni ");
                razon.append("un medio de pago verificado compatible con subastas en ").append(subastaMoneda);
            }
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("No cumplís con los requisitos")
                    .setMessage("No podés ofertar porque no tenés " + razon + ".")
                    .setPositiveButton("Entrar como espectador", (d, w) -> {
                        setSpectatorMode();
                        iniciarTimerYConectar(token);
                    })
                    .setNegativeButton("Salir", (d, w) -> finish())
                    .setCancelable(false)
                    .show();
        } else {
            iniciarTimerYConectar(token);
        }
    }

    private void iniciarTimerYConectar(String token) {
        iniciarTimer();
        conectarWebSocket(token);
    }

    // ────────────────────────────────────────────────────────────────────────
    // WebSocket
    // ────────────────────────────────────────────────────────────────────────

    private void conectarWebSocket(String token) {
        auctionStompClient = new AuctionStompClient();
        auctionStompClient.connect(String.valueOf(subastaId), token, this);
    }

    private void realizarPuja() {
        if (pujaEnProceso) return;
        if (currentItemId == -1) {
            Toast.makeText(this, "Cargando datos, espera...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auctionStompClient == null || !auctionStompClient.isConnected()) {
            Toast.makeText(this, "Sin conexión. Reintentando...", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Enviando puja: " + miPuja + " vía STOMP");
        setBidProcessing(true);
        // Issue 13: timeout para desbloquear el botón si el WebSocket no responde
        bidTimeoutHandler.removeCallbacks(bidTimeoutRunnable != null ? bidTimeoutRunnable : () -> {});
        bidTimeoutRunnable = () -> {
            if (pujaEnProceso) {
                runOnUiThread(() -> {
                    setBidProcessing(false);
                    Toast.makeText(SubastaEnVivoActivity.this,
                            "Sin respuesta del servidor. Verificá si tu puja fue registrada.",
                            Toast.LENGTH_LONG).show();
                });
            }
        };
        bidTimeoutHandler.postDelayed(bidTimeoutRunnable, BID_TIMEOUT_MS);
        auctionStompClient.sendBid(String.valueOf(subastaId), miPuja);
    }

    private void setBidProcessing(boolean processing) {
        pujaEnProceso = processing;
        LinearLayout btnPujar = findViewById(R.id.btn_pujar);
        if (btnPujar == null) return;
        if (processing) {
            btnPujar.setEnabled(false);
            btnPujar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#9A7B3E")));
            if (progressBid != null) progressBid.setVisibility(android.view.View.VISIBLE);
            if (tvBtnPujar != null) tvBtnPujar.setText("Enviando oferta...");
        } else {
            btnPujar.setEnabled(!isSpectator);
            btnPujar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#C0A062")));
            if (progressBid != null) progressBid.setVisibility(android.view.View.GONE);
            actualizarUITextos();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Timer
    // ────────────────────────────────────────────────────────────────────────

    private void iniciarTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        long ahora = System.currentTimeMillis() + clientServerOffset;
        long tiempoRestanteMs = TIMER_TOTAL_DURATION_MS;
        if (limiteFinalizacionEpoch != null) {
            tiempoRestanteMs = limiteFinalizacionEpoch - ahora;
            if (tiempoRestanteMs < 0) tiempoRestanteMs = 0;
        }

        final long duracionTotal = tiempoRestanteMs;
        countDownTimer = new CountDownTimer(duracionTotal, 1000) {
            @Override public void onTick(long ms) {
                if (tvTiempo != null) {
                    tvTiempo.setText(String.format("%02d:%02d", ms / 60000, (ms / 1000) % 60));
                }
                if (progressPuja != null) {
                    int pct = (int) ((ms * 100) / TIMER_TOTAL_DURATION_MS);
                    progressPuja.setProgress(Math.min(100, Math.max(0, pct)));
                }
            }
            @Override public void onFinish() {
                if (tvTiempo != null) tvTiempo.setText("00:00");
                if (progressPuja != null) progressPuja.setProgress(0);
            }
        }.start();
    }

    // ────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Calcula el paso de incremento/decremento para los botones +/-.
     * Para Oro/Platino se usa el 1% del precio base como paso sugerido,
     * aunque no hay mínimo obligatorio.
     */
    private double calcularStep() {
        return Math.max(1.0, precioBase * 0.01);
    }

    private void actualizarUITextos() {
        if (miPuja < siguientePujaMinima) miPuja = siguientePujaMinima;

        if (tvPrecio != null) {
            tvPrecio.setText(formatMoneda(precioActual));
        }
        if (etOfertaInput != null) {
            etOfertaInput.setText(formatearEntero(miPuja));
        }
        if (tvRealizarPujaLabel != null) {
            String labelMin = "mín " + formatMoneda(siguientePujaMinima);
            String labelMax = sinLimiteMaximo ? "sin tope máximo" : "máx " + formatMoneda(siguientePujaMaxima);
            tvRealizarPujaLabel.setText("Realizar una puja (" + labelMin + " · " + labelMax + ")");
        }
        if (tvBtnPujar != null) {
            tvBtnPujar.setText(isSpectator ? "MODO ESPECTADOR" : "OFERTAR");
        }
    }

    private void actualizarUIConPostor(String nombrePostor, int numeroPostor) {
        boolean esBlackwood = numeroPostor == 999
                || "Blackwood Subastas".equalsIgnoreCase(nombrePostor);

        if (tvNombrePostor != null) {
            tvNombrePostor.setText(nombrePostor != null ? nombrePostor : "Blackwood Subastas");
        }
        if (tvAvatarIniciales != null) {
            if (esBlackwood || nombrePostor == null) {
                tvAvatarIniciales.setText("BS");
            } else {
                tvAvatarIniciales.setText(
                        nombrePostor.substring(0, Math.min(2, nombrePostor.length())).toUpperCase());
            }
        }
        if (tvTituloPuja != null) {
            tvTituloPuja.setText(esBlackwood ? "PUJA INICIAL" : "PUJA MÁS ALTA");
        }
        if (tvBadgeLider != null) {
            tvBadgeLider.setVisibility(esBlackwood ? android.view.View.GONE : android.view.View.VISIBLE);
        }
    }

    private void setSpectatorMode() {
        isSpectator = true;
        // Ocultar controles de puja
        LinearLayout controlsContainer = findViewById(R.id.btn_decrementar) != null
                ? (LinearLayout) findViewById(R.id.btn_decrementar).getParent() : null;
        if (controlsContainer != null) {
            controlsContainer.setVisibility(android.view.View.GONE);
        }
        if (tvBtnPujar != null) tvBtnPujar.setText("MODO ESPECTADOR");
        LinearLayout btnPujar = findViewById(R.id.btn_pujar);
        if (btnPujar != null) {
            btnPujar.setEnabled(false);
            btnPujar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.GRAY));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Navegación y ciclo de vida
    // ────────────────────────────────────────────────────────────────────────

    private void configurarBottomNav() {
        int[] tabs = { R.id.tab_subastas, R.id.tab_mis_pujas, R.id.tab_perfil };
        int[] indices = { 0, 1, 2 };
        for (int i = 0; i < tabs.length; i++) {
            final int tabIndex = indices[i];
            LinearLayout tab = findViewById(tabs[i]);
            if (tab != null) tab.setOnClickListener(v -> irAHome(tabIndex));
        }
    }

    private void irAHome(int tabIndex) {
        Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
        intent.putExtra("TAB_INDEX", tabIndex);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (bidTimeoutRunnable != null) bidTimeoutHandler.removeCallbacks(bidTimeoutRunnable);
        if (auctionStompClient != null) auctionStompClient.disconnect();
        // Notificar al backend que el usuario salió
        RetrofitClient.getApiService().salirSubasta(subastaId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {}
                    @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
                });
    }

    // ────────────────────────────────────────────────────────────────────────
    // AuctionStompClient.AuctionEventListener
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void onConnected() {
        runOnUiThread(() -> Toast.makeText(this, "Conectado a la sala en vivo", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStateUpdate(AuctionStateUpdate update) {
        runOnUiThread(() -> {
            // Puja confirmada (propia o de otro postor) → cancelar timeout y liberar botón
            if (bidTimeoutRunnable != null) bidTimeoutHandler.removeCallbacks(bidTimeoutRunnable);
            setBidProcessing(false);

            precioActual = update.currentPrice;
            limiteFinalizacionEpoch = update.endEpochMillis;
            currentItemId = update.lotNumber;

            siguientePujaMinima = sinLimiteMaximo
                    ? precioActual + 1.0
                    : precioActual + precioBase * 0.01;
            siguientePujaMaxima = sinLimiteMaximo
                    ? Double.MAX_VALUE
                    : precioActual + precioBase * 0.20;
            miPuja = siguientePujaMinima;

            String nombrePostor = update.topBidderName != null ? update.topBidderName : "Blackwood Subastas";
            int numPostor = nombrePostor.equalsIgnoreCase("Blackwood Subastas") ? 999 : 1;
            actualizarUIConPostor(nombrePostor, numPostor);
            actualizarUITextos();
            iniciarTimer();

            Toast.makeText(this, "¡Nueva puja: " + formatMoneda(precioActual) + "!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onUsersUpdate(AuctionUsersUpdate update) {
        runOnUiThread(() -> {
            if (tvParticipantesCount != null) {
                tvParticipantesCount.setText(update.count + " en línea");
            }
            if (layoutAvatars != null) {
                layoutAvatars.removeAllViews();
                int density = (int) getResources().getDisplayMetrics().density;
                int sizePx   = 36 * density;
                int marginPx = 8 * density;

                for (AuctionUsersUpdate.UserInfo p : update.participants) {
                    TextView tv = new TextView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
                    params.setMarginEnd(marginPx);
                    tv.setLayoutParams(params);
                    tv.setGravity(android.view.Gravity.CENTER);
                    tv.setTextSize(11);
                    tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    tv.setText(p.initials != null ? p.initials : "?");

                    boolean esMio = p.username.equalsIgnoreCase(currentUsername);
                    if (esMio) {
                        // Mi propio badge: verde oscuro con borde dorado
                        tv.setBackgroundResource(R.drawable.circle_black_gold_border);
                        tv.setTextColor(Color.parseColor("#C6A75E"));
                    } else {
                        // Otros participantes: verde oscuro de la app
                        tv.setBackgroundResource(R.drawable.circle_dark);
                        tv.setTextColor(Color.parseColor("#C6A75E"));
                    }
                    layoutAvatars.addView(tv);
                }
            }
        });
    }

    @Override
    public void onLotChange(LotChangeUpdate update) {
        runOnUiThread(() -> {
            int soldDisplay = update.soldLotOrder > 0 ? update.soldLotOrder : update.soldLotNumber;

            // Issue 15: detectar si el usuario actual ganó el lote anterior
            boolean usuarioGano = !isSpectator
                    && update.soldLotWinnerName != null
                    && !update.soldLotWinnerName.isEmpty()
                    && (update.soldLotWinnerName.equalsIgnoreCase(currentUserNombre)
                        || update.soldLotWinnerName.equalsIgnoreCase(currentUsername));

            String msg = "Lote #" + soldDisplay + " vendido → " + update.soldLotWinnerName
                    + " por " + formatMoneda(update.soldLotFinalPrice);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            // Actualizar con los datos del nuevo lote
            currentItemId = update.newLotNumber;
            precioActual  = update.newLotStartingPrice;
            precioBase    = update.newLotStartingPrice;
            limiteFinalizacionEpoch = update.newLotEndEpochMillis;

            siguientePujaMinima = sinLimiteMaximo
                    ? precioActual + 1.0
                    : precioActual + precioBase * 0.01;
            siguientePujaMaxima = sinLimiteMaximo
                    ? Double.MAX_VALUE
                    : precioActual + precioBase * 0.20;
            miPuja = siguientePujaMinima;

            int displayOrder = update.newLotOrder > 0 ? update.newLotOrder : update.newLotNumber;
            if (tvLoteBadge != null) tvLoteBadge.setText(String.format("LOTE #%03d", displayOrder));
            if (tvLoteTitulo != null) {
                tvLoteTitulo.setText(update.newLotTitle != null ? update.newLotTitle : "Lote sin título");
            }
            if (tvLoteDescripcion != null) {
                tvLoteDescripcion.setText(update.newLotDescription != null ? update.newLotDescription : "");
            }
            if (ivLoteImagen != null && update.newLotImageUrl != null) {
                String imageUrl = RetrofitClient.BASE_URL
                        + update.newLotImageUrl.replaceAll("^/", "");
                Glide.with(this).load(imageUrl)
                        .signature(new ObjectKey("item-" + currentItemId))
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivLoteImagen);
            }

            // Ocultar info de obra de arte hasta cargar la del nuevo lote
            if (cardVivoObraArte != null) cardVivoObraArte.setVisibility(View.GONE);
            // Issue 9: cargar datos de obra de arte para el nuevo lote
            cargarInfoObraArteDeItem(currentItemId);

            actualizarUIConPostor(update.newLotStartingBidder, 999);
            actualizarUITextos();
            iniciarTimer();

            // Issue 15: mostrar modal si el usuario ganó el lote que acaba de cerrar
            if (usuarioGano) {
                mostrarModalGanador(soldDisplay);
            }
        });
    }

    private void mostrarModalGanador(int loteNumero) {
        new AlertDialog.Builder(this)
                .setTitle("¡Felicitaciones!")
                .setMessage("Ganaste el Lote #" + loteNumero + ". ¿Qué querés hacer?")
                .setPositiveButton("Ir a pagar", (dialog, which) -> {
                    if (auctionStompClient != null) auctionStompClient.disconnect();
                    irAHome(1);
                })
                .setNegativeButton("Seguir en la subasta", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    @SuppressWarnings("unchecked")
    private void cargarInfoObraArte(int productoId) {
        RetrofitClient.getApiService().getProductoDetalle(productoId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarInfoObraArte(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
    }

    private void cargarInfoObraArteDeItem(int itemId) {
        RetrofitClient.getApiService().getItemDetalle(itemId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Object pidObj = response.body().get("productoId");
                            if (pidObj instanceof Number) {
                                cargarInfoObraArte(((Number) pidObj).intValue());
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
    }

    private void mostrarInfoObraArte(Map<String, Object> producto) {
        runOnUiThread(() -> {
            String tipo = (String) producto.get("tipo");
            if ("OBRA_ARTE".equals(tipo) && cardVivoObraArte != null) {
                String artista = (String) producto.get("artista");
                String historia = (String) producto.get("historia");
                if (tvVivoObraArtista != null)
                    tvVivoObraArtista.setText(artista != null ? artista : "-");
                if (tvVivoObraHistoria != null)
                    tvVivoObraHistoria.setText(historia != null ? historia : "");
                cardVivoObraArte.setVisibility(View.VISIBLE);
            } else if (cardVivoObraArte != null) {
                cardVivoObraArte.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAuctionFinished(AuctionFinishedUpdate update) {
        runOnUiThread(() -> {
            Toast.makeText(this,
                    update.message != null ? update.message : "La subasta ha finalizado, gracias por participar",
                    Toast.LENGTH_LONG).show();
            // Navegar al detalle de la subasta para que se muestre como FINALIZADA
            Intent intent = new Intent(this, DetalleSubastaActivity.class);
            intent.putExtra("SUBASTA_ID", subastaId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBidError(BidError error) {
        runOnUiThread(() -> {
            // Puja rechazada → cancelar timeout y liberar botón
            if (bidTimeoutRunnable != null) bidTimeoutHandler.removeCallbacks(bidTimeoutRunnable);
            setBidProcessing(false);

            Toast.makeText(this,
                    "Puja rechazada: " + error.reason + ". Mínimo: " + formatMoneda(error.minimumRequired),
                    Toast.LENGTH_LONG).show();
            if (error.minimumRequired > 0) {
                siguientePujaMinima = error.minimumRequired;
                miPuja = siguientePujaMinima;
                actualizarUITextos();
            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Conexión STOMP finalizada.");
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, "Error WebSocket: " + message);
        runOnUiThread(() -> Toast.makeText(this, "Error de conexión: " + message, Toast.LENGTH_SHORT).show());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ────────────────────────────────────────────────────────────────────────

    private void sincronizarReloj(Response<?> response) {
        String dateHeader = response.headers().get("Date");
        if (dateHeader != null) {
            try {
                long serverTime = java.util.Date.parse(dateHeader);
                clientServerOffset = serverTime - System.currentTimeMillis();
            } catch (Exception ignored) {}
        }
    }

    private double parseInput() {
        if (etOfertaInput == null) return miPuja;
        try {
            String text = etOfertaInput.getText().toString().replaceAll("[^0-9.]", "");
            return text.isEmpty() ? miPuja : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return miPuja;
        }
    }

    /** Formatea un monto con símbolo de moneda. */
    private String formatMoneda(double valor) {
        String simbolo = "USD".equalsIgnoreCase(subastaMoneda) ? "USD " : "$ ";
        return simbolo + formatearEntero(valor);
    }

    /** Formatea un double como entero con puntos de miles (ej: 1.234.567). */
    private String formatearEntero(double valor) {
        String s = String.valueOf((long) valor);
        StringBuilder sb = new StringBuilder();
        int start = s.length() % 3;
        if (start > 0) sb.append(s, 0, start);
        for (int i = start; i < s.length(); i += 3) {
            if (sb.length() > 0) sb.append('.');
            sb.append(s, i, i + 3);
        }
        return sb.toString();
    }

    private int getCategoryRank(String cat) {
        if (cat == null) return 1;
        switch (cat.toLowerCase().trim()) {
            case "especial": return 2;
            case "plata":    return 3;
            case "oro":      return 4;
            case "platino":  return 5;
            default:         return 1;
        }
    }

    // ── Map helpers ──────────────────────────────────────────────────────────

    private String getStr(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return (v instanceof String) ? (String) v : def;
    }

    private int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        return (v instanceof Number) ? ((Number) v).intValue() : def;
    }

    private double getDouble(Map<String, Object> map, String key, double def) {
        Object v = map.get(key);
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    private boolean getBool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        return (v instanceof Boolean) ? (Boolean) v : def;
    }
}
