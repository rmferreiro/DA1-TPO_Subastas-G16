package tpo.g16.blackwood.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.R;
import tpo.g16.blackwood.network.RetrofitClient;

public class CrearSubastaActivity extends AppCompatActivity {

    private EditText etFecha, etHora, etUbicacion, etRematador, etDescripcion;
    private TextView btnMonedaArs, btnMonedaUsd;
    private Spinner spCategoria;
    private MaterialButton btnSeleccionarLotes;
    private TextView txtLotesTitulo;
    private LinearLayout layoutLotesSeleccionados;
    private MaterialButton btnCrearSubasta;

    private String monedaSeleccionada = "ARS";
    private Calendar calendar = Calendar.getInstance();

    // Lotes cargados desde el servidor
    private List<Map<String, Object>> lotesDisponibles = new ArrayList<>();
    // Lotes seleccionados por el administrador
    private List<Map<String, Object>> lotesSeleccionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_subasta);

        // Header Title / Subtitle
        TextView headerSubtitle = findViewById(R.id.header_subtitle);
        if (headerSubtitle != null) {
            headerSubtitle.setText("Crear nueva subasta");
        }

        // Vincular componentes
        etFecha = findViewById(R.id.et_subasta_fecha);
        etHora = findViewById(R.id.et_subasta_hora);
        etUbicacion = findViewById(R.id.et_subasta_ubicacion);
        etRematador = findViewById(R.id.et_subasta_rematador);
        etDescripcion = findViewById(R.id.et_subasta_descripcion);

        btnMonedaArs = findViewById(R.id.btn_subasta_moneda_ars);
        btnMonedaUsd = findViewById(R.id.btn_subasta_moneda_usd);

        spCategoria = findViewById(R.id.sp_subasta_categoria);
        btnSeleccionarLotes = findViewById(R.id.btn_seleccionar_lotes);
        txtLotesTitulo = findViewById(R.id.txt_lotes_seleccionados_titulo);
        layoutLotesSeleccionados = findViewById(R.id.layout_lotes_seleccionados);
        btnCrearSubasta = findViewById(R.id.btn_crear_subasta);

        // Configurar selector de fecha
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        // Configurar selector de hora
        etHora.setOnClickListener(v -> mostrarTimePicker());

        // Configurar selector de moneda
        btnMonedaArs.setOnClickListener(v -> alternarMoneda("ARS"));
        btnMonedaUsd.setOnClickListener(v -> alternarMoneda("USD"));
        alternarMoneda("ARS"); // Default

        // Configurar spinner de categoria
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Común", "Especial", "Plata", "Oro", "Platino"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);

        // Configurar listeners de validación en tiempo real
        TextWatcher validationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validarFormulario();
            }
        };
        etUbicacion.addTextChangedListener(validationWatcher);
        etRematador.addTextChangedListener(validationWatcher);
        etDescripcion.addTextChangedListener(validationWatcher);

        btnSeleccionarLotes.setOnClickListener(v -> abrirModalLotes());
        btnCrearSubasta.setOnClickListener(v -> crearSubasta());

        cargarLotesDisponibles();
    }

    private void mostrarDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateStr = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etFecha.setText(dateStr);
                    validarFormulario();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String timeStr = String.format("%02d:%02d", selectedHour, selectedMinute);
                    etHora.setText(timeStr);
                    validarFormulario();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void alternarMoneda(String moneda) {
        monedaSeleccionada = moneda;
        if ("ARS".equals(moneda)) {
            btnMonedaArs.setBackgroundResource(R.drawable.segment_active_bg);
            btnMonedaArs.setTextColor(Color.WHITE);
            btnMonedaUsd.setBackgroundColor(Color.TRANSPARENT);
            btnMonedaUsd.setTextColor(Color.parseColor("#1C2A21"));
        } else {
            btnMonedaUsd.setBackgroundResource(R.drawable.segment_active_bg);
            btnMonedaUsd.setTextColor(Color.WHITE);
            btnMonedaArs.setBackgroundColor(Color.TRANSPARENT);
            btnMonedaArs.setTextColor(Color.parseColor("#1C2A21"));
        }

        // Limpiar lotes seleccionados de la moneda anterior
        lotesSeleccionados.clear();
        layoutLotesSeleccionados.removeAllViews();
        txtLotesTitulo.setVisibility(View.GONE);
        validarFormulario();
    }

    private void cargarLotesDisponibles() {
        RetrofitClient.getApiService().getProductosAprobados()
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            lotesDisponibles = response.body();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        Toast.makeText(CrearSubastaActivity.this, "Error al cargar lotes desde el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirModalLotes() {
        // Filtrar lotes disponibles por la moneda seleccionada
        final List<Map<String, Object>> lotesFiltrados = new ArrayList<>();
        for (Map<String, Object> lote : lotesDisponibles) {
            String monedaLote = (String) lote.get("moneda");
            if (monedaSeleccionada.equalsIgnoreCase(monedaLote)) {
                lotesFiltrados.add(lote);
            }
        }

        if (lotesFiltrados.isEmpty()) {
            Toast.makeText(this, "No hay lotes para la moneda seleccionada", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombres = new String[lotesFiltrados.size()];
        boolean[] marcados = new boolean[lotesFiltrados.size()];

        for (int i = 0; i < lotesFiltrados.size(); i++) {
            Map<String, Object> lote = lotesFiltrados.get(i);
            String titulo = (String) lote.get("descripcion");
            Double precio = (Double) lote.get("precioBasePropuesto");
            nombres[i] = (titulo != null ? titulo : "Lote sin título") + " (" + monedaSeleccionada + " " + String.format("%.2f", precio != null ? precio : 0.0) + ")";
            
            // Verificar si ya estaba seleccionado para mantener el estado
            marcados[i] = lotesSeleccionados.contains(lote);
        }

        final List<Map<String, Object>> seleccionTemporal = new ArrayList<>(lotesSeleccionados);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar lotes (" + monedaSeleccionada + ")");
        builder.setMultiChoiceItems(nombres, marcados, (dialog, which, isChecked) -> {
            Map<String, Object> lote = lotesFiltrados.get(which);
            if (isChecked) {
                if (!seleccionTemporal.contains(lote)) {
                    seleccionTemporal.add(lote);
                }
            } else {
                seleccionTemporal.remove(lote);
            }
        });

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            lotesSeleccionados = seleccionTemporal;
            actualizarLotesVisuales();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void actualizarLotesVisuales() {
        layoutLotesSeleccionados.removeAllViews();
        if (lotesSeleccionados.isEmpty()) {
            txtLotesTitulo.setVisibility(View.GONE);
        } else {
            txtLotesTitulo.setVisibility(View.VISIBLE);
            for (Map<String, Object> lote : lotesSeleccionados) {
                TextView tv = new TextView(this);
                tv.setText("· " + lote.get("descripcion"));
                tv.setTextColor(Color.parseColor("#1C2A21"));
                tv.setTextSize(14f);
                tv.setPadding(0, 4, 0, 4);
                layoutLotesSeleccionados.addView(tv);
            }
        }
        validarFormulario();
    }

    private void validarFormulario() {
        boolean camposLenos = !etFecha.getText().toString().trim().isEmpty() &&
                !etHora.getText().toString().trim().isEmpty() &&
                !etUbicacion.getText().toString().trim().isEmpty() &&
                !etRematador.getText().toString().trim().isEmpty() &&
                !etDescripcion.getText().toString().trim().isEmpty() &&
                !lotesSeleccionados.isEmpty();

        btnCrearSubasta.setEnabled(camposLenos);

        if (camposLenos) {
            btnCrearSubasta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C6A75E"))); // Dorado
            btnCrearSubasta.setTextColor(Color.WHITE);
        } else {
            btnCrearSubasta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // Gris claro
            btnCrearSubasta.setTextColor(Color.parseColor("#757575")); // Gris oscuro
        }
    }

    private void crearSubasta() {
        btnCrearSubasta.setEnabled(false);

        // Armar el request body
        Map<String, Object> request = new HashMap<>();
        request.put("fecha", etFecha.getText().toString().trim());
        request.put("hora", etHora.getText().toString().trim());
        request.put("moneda", monedaSeleccionada);
        request.put("ubicacion", etUbicacion.getText().toString().trim());
        request.put("rematador", etRematador.getText().toString().trim());
        
        // Categoria del Spinner (Común, Oro, Platino, Diamante)
        // El backend espera la categoría correspondiente
        String catValue = spCategoria.getSelectedItem().toString().toUpperCase();
        if ("COMÚN".equals(catValue)) {
            catValue = "COMUN";
        }
        request.put("categoria", catValue);
        request.put("descripcion", etDescripcion.getText().toString().trim());

        List<Integer> lotesIds = new ArrayList<>();
        for (Map<String, Object> lote : lotesSeleccionados) {
            Double idDouble = (Double) lote.get("id");
            if (idDouble != null) {
                lotesIds.add(idDouble.intValue());
            }
        }
        request.put("lotes", lotesIds);

        RetrofitClient.getApiService().crearSubasta(request)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CrearSubastaActivity.this, "Subasta creada", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            btnCrearSubasta.setEnabled(true);
                            try {
                                String errorStr = response.errorBody().string();
                                org.json.JSONObject json = new org.json.JSONObject(errorStr);
                                String msg = json.optString("mensaje", json.optString("message", "Error al crear subasta"));
                                Toast.makeText(CrearSubastaActivity.this, msg, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(CrearSubastaActivity.this, "Error al crear subasta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnCrearSubasta.setEnabled(true);
                        Toast.makeText(CrearSubastaActivity.this, "Falla de red al conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
