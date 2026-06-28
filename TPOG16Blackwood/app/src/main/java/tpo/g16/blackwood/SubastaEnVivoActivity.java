package tpo.g16.blackwood;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.URL;
import java.io.InputStream;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.PujaRequest;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class SubastaEnVivoActivity extends AppCompatActivity {

    private static final String TAG = "SubastaEnVivoActivity";
    private String wsUrl;

    private int subastaId;
    private int currentItemId = -1;
    private Long miMedioPagoId = null;

    private int precioActual = 0;
    private int miPuja = 0;
    private int precioBaseValue = 1000;
    private boolean isOroOrPlatino = true; // Por defecto true para no bloquear erróneamente

    private ImageView ivLoteImagen;
    private ProgressBar progressPuja;
    private TextView tvPrecio, tvMiPuja, tvOferta, tvBtnPujar, tvTiempo;
    private TextView tvLoteNumero, tvLoteTitulo, tvLoteDescripcion;
    private TextView tvAvatarIniciales, tvNombrePostor, tvParticipantesCount;

    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private CountDownTimer countDownTimer;
    private static final long TIEMPO_SUBASTA_MS = 60 * 1000; // 60 segundos

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

        // Registrar contexto para el cliente Retrofit estático
        tpo.g16.blackwood.network.RetrofitClient.getInstance(this);

        // Armar URL de WebSockets dinámicamente según la IP configurada
        wsUrl = tpo.g16.blackwood.network.ApiConfig.BASE_URL
                .replace("http://", "ws://")
                .replace("https://", "wss://") + "ws/subasta/websocket";

        tvPrecio = findViewById(R.id.tv_precio_actual);
        tvOferta = findViewById(R.id.tv_oferta_input);
        tvBtnPujar = findViewById(R.id.tv_btn_pujar);
        progressPuja = findViewById(R.id.progress_puja);
        tvTiempo = findViewById(R.id.tv_tiempo);
        ivLoteImagen = findViewById(R.id.iv_lote_imagen);

        tvLoteNumero = findViewById(R.id.tv_lote_numero);
        tvLoteTitulo = findViewById(R.id.tv_lote_titulo);
        tvLoteDescripcion = findViewById(R.id.tv_lote_descripcion);
        tvAvatarIniciales = findViewById(R.id.tv_avatar_iniciales);
        tvNombrePostor = findViewById(R.id.tv_nombre_postor);
        tvParticipantesCount = findViewById(R.id.tv_participantes_count);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        LinearLayout btnDecrementar = findViewById(R.id.btn_decrementar);
        if (btnDecrementar != null) {
            btnDecrementar.setOnClickListener(v -> {
                int incrementoMinimo = Math.max(1, (int)(precioBaseValue * 0.01));
                if (miPuja > precioActual + incrementoMinimo) {
                    miPuja -= incrementoMinimo; // Bajar de a saltos del 1%
                    actualizarUITextos();
                }
            });
        }

        LinearLayout btnIncrementar = findViewById(R.id.btn_incrementar);
        if (btnIncrementar != null) {
            btnIncrementar.setOnClickListener(v -> {
                int incrementoMinimo = Math.max(1, (int)(precioBaseValue * 0.01));
                int limiteMaximo = precioActual + (int)(precioBaseValue * 0.20);
                
                // Si es Oro o Platino no hay limite maximo (para simplificar en frontend, si es >= maximo lo dejamos subir)
                // Pero si queremos ser estrictos para comun/plata, limitamos:
                if (isOroOrPlatino || miPuja + incrementoMinimo <= limiteMaximo) {
                    miPuja += incrementoMinimo;
                } else if (!isOroOrPlatino && miPuja < limiteMaximo) {
                    miPuja = limiteMaximo; // Toparlo al 20%
                }
                actualizarUITextos();
            });
        }
        

        LinearLayout btnPujar = findViewById(R.id.btn_pujar);
        if (btnPujar != null) {
            btnPujar.setOnClickListener(v -> {
                realizarPuja(miPuja);
            });
        }

        configurarBottomNav();

        compositeDisposable = new CompositeDisposable();

        // 1. Obtener Medio de Pago
        fetchMedioPago();
    }

    private void fetchMedioPago() {
        RetrofitClient.getApiService().getMediosPago().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (Map<String, Object> mp : response.body()) {
                        Boolean verificado = (Boolean) mp.get("verificado");
                        Boolean activo = (Boolean) mp.get("activo");
                        if (Boolean.TRUE.equals(verificado) && Boolean.TRUE.equals(activo)) {
                            miMedioPagoId = ((Number) mp.get("id")).longValue();
                            break;
                        }
                    }
                    if (miMedioPagoId == null) {
                        Toast.makeText(SubastaEnVivoActivity.this, "Modo Espectador: Solo podés ver la subasta", Toast.LENGTH_LONG).show();
                        android.view.ViewParent parent = findViewById(R.id.btn_decrementar).getParent();
                        if (parent instanceof LinearLayout) {
                            ((LinearLayout)parent).setVisibility(android.view.View.GONE);
                        }
                        if (tvBtnPujar != null) {
                            tvBtnPujar.setText("MODO ESPECTADOR");
                        }
                        if (findViewById(R.id.btn_pujar) != null) {
                            findViewById(R.id.btn_pujar).setEnabled(false);
                            findViewById(R.id.btn_pujar).setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
                        }
                    }
                    // 2. Obtener el item actual
                    fetchItemActual();
                } else {
                    Toast.makeText(SubastaEnVivoActivity.this, "Error obteniendo medio de pago", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(SubastaEnVivoActivity.this, "Error de red MP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchItemActual() {
        RetrofitClient.getApiService().getItemsDisponibles(subastaId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(SubastaEnVivoActivity.this, "Subasta finalizada. No hay más lotes.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SubastaEnVivoActivity.this, tpo.g16.blackwood.main.HomeActivity.class);
                        intent.putExtra("TAB_INDEX", 1);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    Map<String, Object> item = response.body().get(0);
                    
                    Object itemIdObj = item.get("itemId");
                    if (itemIdObj != null) {
                        currentItemId = ((Number) itemIdObj).intValue();
                    } else {
                        Toast.makeText(SubastaEnVivoActivity.this, "Error de datos del item", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    
                    String descCompleta = (String) item.get("descripcion");
                    String descCatalogo = (String) item.get("descripcionCatalogo");
                    
                    if (tvLoteNumero != null) tvLoteNumero.setText("LOTE #" + currentItemId);
                    if (tvLoteTitulo != null) tvLoteTitulo.setText(descCompleta != null ? descCompleta : "Producto sin título");
                    if (tvLoteDescripcion != null) tvLoteDescripcion.setText(descCatalogo != null ? descCatalogo : "");

                    // Cargar imagen dinámicamente con Glide
                    if (ivLoteImagen != null && item.get("productoId") != null) {
                        int prodId = ((Number) item.get("productoId")).intValue();
                        String imageUrl = tpo.g16.blackwood.network.RetrofitClient.BASE_URL + "api/productos/" + prodId + "/foto";
                        com.bumptech.glide.Glide.with(SubastaEnVivoActivity.this)
                                .load(imageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(ivLoteImagen);
                    }

                    fetchMejorPuja();
                } else {
                    Toast.makeText(SubastaEnVivoActivity.this, "Error obteniendo lotes", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(SubastaEnVivoActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMejorPuja() {
        if (currentItemId == -1) return;
        RetrofitClient.getApiService().getMejorPuja(subastaId, currentItemId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    if (body.containsKey("importe") && body.get("importe") != null) {
                        precioActual = ((Number) body.get("importe")).intValue();
                    } else if (body.containsKey("precioBase") && body.get("precioBase") != null) {
                        precioActual = ((Number) body.get("precioBase")).intValue();
                    } else {
                        precioActual = 1000;
                    }
                    
                    if (body.containsKey("precioBase") && body.get("precioBase") != null) {
                        precioBaseValue = ((Number) body.get("precioBase")).intValue();
                    }
                    
                    int incrementoMinimo = Math.max(1, (int)(precioBaseValue * 0.01));
                    miPuja = precioActual + incrementoMinimo;
                    actualizarUITextos();
                    
                    // Iniciar timer inmediatamente (por si el WS tarda o falla)
                    iniciarTimer();
                    
                    // Conectar a WebSockets después de inicializar
                    connectWebSocket();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(SubastaEnVivoActivity.this, "Error obteniendo precio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(TIEMPO_SUBASTA_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvTiempo != null) {
                    long minutos = (millisUntilFinished / 1000) / 60;
                    long segundos = (millisUntilFinished / 1000) % 60;
                    tvTiempo.setText(String.format("%02d:%02d", minutos, segundos));
                }
                if (progressPuja != null) {
                    int pct = (int) ((millisUntilFinished * 100) / TIEMPO_SUBASTA_MS);
                    progressPuja.setProgress(pct);
                }
            }

            @Override
            public void onFinish() {
                if (tvTiempo != null) tvTiempo.setText("00:00");
                if (progressPuja != null) progressPuja.setProgress(0);
                Toast.makeText(SubastaEnVivoActivity.this, "Subasta finalizada", Toast.LENGTH_LONG).show();
                
                LinearLayout btnPujar = findViewById(R.id.btn_pujar);
                if (btnPujar != null) {
                    btnPujar.setEnabled(false);
                    btnPujar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
                }
                if (tvBtnPujar != null) {
                    tvBtnPujar.setText("Finalizada");
                }
                
                // Llamar al backend para cerrar el item y declarar ganador oficial
                if (currentItemId != -1) {
                    RetrofitClient.getApiService().cerrarItem(subastaId, currentItemId).enqueue(new retrofit2.Callback<tpo.g16.blackwood.network.models.PujaResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<tpo.g16.blackwood.network.models.PujaResponse> call, retrofit2.Response<tpo.g16.blackwood.network.models.PujaResponse> response) {
                            verificarResultadoItem(currentItemId);
                        }

                        @Override
                        public void onFailure(retrofit2.Call<tpo.g16.blackwood.network.models.PujaResponse> call, Throwable t) {
                            verificarResultadoItem(currentItemId);
                        }
                    });
                }
            }
        }.start();
    }

    private void verificarResultadoItem(int itemId) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            RetrofitClient.getApiService().getItemResultado(subastaId, itemId).enqueue(new retrofit2.Callback<tpo.g16.blackwood.network.models.ItemResultadoResponse>() {
                @Override
                public void onResponse(retrofit2.Call<tpo.g16.blackwood.network.models.ItemResultadoResponse> call, retrofit2.Response<tpo.g16.blackwood.network.models.ItemResultadoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tpo.g16.blackwood.network.models.ItemResultadoResponse res = response.body();
                        if (res.isSoyGanador()) {
                            Intent intent = new Intent(SubastaEnVivoActivity.this, PermanecerSubastaActivity.class);
                            intent.putExtra("productoDesc", res.getProductoDesc());
                            intent.putExtra("subastaDesc", res.getSubastaDesc());
                            if (res.getImporte() != null) {
                                intent.putExtra("importe", res.getImporte().doubleValue());
                            }
                            if (res.getPujoId() != null) {
                                intent.putExtra("pujoId", res.getPujoId());
                            }
                            intent.putExtra("SUBASTA_ID", subastaId);
                            intent.putExtra("ITEM_ID", itemId);
                            startActivity(intent);
                            finish();
                        } else {
                            if (res.getGanadorNombre() != null && !res.getGanadorNombre().equals("Nadie")) {
                                Toast.makeText(SubastaEnVivoActivity.this, "Lote vendido a " + res.getGanadorNombre(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SubastaEnVivoActivity.this, "Lote finalizado (Sin pujas)", Toast.LENGTH_SHORT).show();
                            }
                            fetchItemActual();
                        }
                    } else {
                        fetchItemActual();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<tpo.g16.blackwood.network.models.ItemResultadoResponse> call, Throwable t) {
                    fetchItemActual();
                }
            });
        }, 500);
    }

    @SuppressLint("CheckResult")
    private void connectWebSocket() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);
        
        compositeDisposable.add(stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "Stomp connection opened");
                            runOnUiThread(() -> {
                                Toast.makeText(SubastaEnVivoActivity.this, "Conectado a la sala en vivo", Toast.LENGTH_SHORT).show();
                            });
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d(TAG, "Stomp connection closed");
                            break;
                    }
                }));

        // Suscribirse a los broadcasts de la subasta
        compositeDisposable.add(stompClient.topic("/topic/subasta/" + subastaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received message: " + topicMessage.getPayload());
                    JsonObject json = new Gson().fromJson(topicMessage.getPayload(), JsonObject.class);
                    
                    if (json.has("itemId") && json.get("itemId").getAsInt() == currentItemId) {
                        if (json.has("importe")) {
                            precioActual = json.get("importe").getAsInt();
                            int incrementoMinimo = Math.max(1, (int)(precioBaseValue * 0.01));
                            miPuja = precioActual + incrementoMinimo;
                            
                            String nombrePostor = json.has("nombrePostor") ? json.get("nombrePostor").getAsString() : "Postor Anonimo";
                            if (tvNombrePostor != null) tvNombrePostor.setText(nombrePostor);
                            if (tvAvatarIniciales != null && nombrePostor.length() > 0) {
                                tvAvatarIniciales.setText(nombrePostor.substring(0, 1).toUpperCase());
                            }
                            
                            // Reiniciar el timer con cada nueva puja para dar tiempo a otros
                            iniciarTimer();
                            
                            actualizarUITextos();
                            Toast.makeText(this, "¡Nueva puja de $" + precioActual + "!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, throwable -> {
                    Log.e(TAG, "Error en topic", throwable);
                }));

        stompClient.connect();
    }

    private void realizarPuja(int importe) {
        if (currentItemId == -1 || miMedioPagoId == null) {
            Toast.makeText(this, "Cargando datos, espera...", Toast.LENGTH_SHORT).show();
            return;
        }

        PujaRequest req = new PujaRequest(currentItemId, importe, miMedioPagoId);
        
        // Bloquear UI temporalmente opcional (ej: cambiar color boton)
        
        RetrofitClient.getApiService().pujar(subastaId, req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Puja enviada correctamente. Esperando broadcast WS.");
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Error";
                        Toast.makeText(SubastaEnVivoActivity.this, "Puja rechazada: " + err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {}
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(SubastaEnVivoActivity.this, "Error de red al pujar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUITextos() {
        int incrementoMinimo = Math.max(1, (int)(precioBaseValue * 0.01));
        if (miPuja <= precioActual) {
            miPuja = precioActual + incrementoMinimo;
        }

        String montoStr = "$ " + formatear(miPuja);
        String precioStr = "$ " + formatear(precioActual);

        if (tvPrecio != null) tvPrecio.setText(precioStr + " USD");
        if (tvOferta != null) tvOferta.setText(montoStr);
        if (tvBtnPujar != null) tvBtnPujar.setText("Ofertar " + montoStr + " USD");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (compositeDisposable != null) compositeDisposable.dispose();
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
        // Llamar API de salir
        RetrofitClient.getApiService().salirSubasta(subastaId).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {}
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void configurarBottomNav() {
        LinearLayout tabSubastas = findViewById(R.id.tab_subastas);
        LinearLayout tabPujas = findViewById(R.id.tab_mis_pujas);
        LinearLayout tabPerfil = findViewById(R.id.tab_perfil);
        if (tabSubastas != null) {
            tabSubastas.setOnClickListener(v -> {
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
        if (tabPujas != null) {
            tabPujas.setOnClickListener(v -> {
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
        if (tabPerfil != null) {
            tabPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(this, tpo.g16.blackwood.main.HomeActivity.class);
                intent.putExtra("TAB_INDEX", 2);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}
