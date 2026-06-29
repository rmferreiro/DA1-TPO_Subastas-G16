package tpo.g16.blackwood;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;

public class ConfirmarPagoActivity extends AppCompatActivity {

    private int itemId = -1;
    private double oferta = 0.0;
    private double comision = 0.0;
    private double costoEnvio = 0.0;
    private double totalAPagar = 0.0;
    private String descripcion = "";

    /** Moneda de la subasta: "ARS" o "USD" — se carga al inicio. */
    private String subastaMoneda = "ARS";

    private TextView tvProductoDesc, tvOferta, tvComisionLabel, tvComision, tvCostoEnvio, tvTotal, tvErrorMediosPago;
    private View rowCostoEnvio;
    private Spinner spinnerMediosPago;
    private List<Long> mediosPagoIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_pago);

        itemId      = getIntent().getIntExtra("ITEM_ID", -1);
        oferta      = getIntent().getDoubleExtra("OFERTA", 0.0);
        comision    = getIntent().getDoubleExtra("COMISION", 0.0);
        costoEnvio  = getIntent().getDoubleExtra("COSTO_ENVIO", 0.0);
        totalAPagar = getIntent().getDoubleExtra("TOTAL_A_PAGAR", 0.0);
        descripcion = getIntent().getStringExtra("DESCRIPCION");
        // Si el lanzador ya conoce la moneda, la recibe por intent; de lo contrario la cargamos
        String monedaIntent = getIntent().getStringExtra("MONEDA");
        if (monedaIntent != null && !monedaIntent.isEmpty()) {
            subastaMoneda = monedaIntent;
        }

        initViews();
        configurarListeners();

        if (itemId != -1) {
            cargarDatos();
            if (monedaIntent != null && !monedaIntent.isEmpty()) {
                // Moneda ya disponible → cargar medios de pago directamente
                cargarMediosPagoFiltrados();
            } else {
                // Obtener moneda desde la API y luego cargar medios
                cargarMonedaYMediosPago();
            }
        } else {
            Toast.makeText(this, "Error: Item no especificado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvProductoDesc  = findViewById(R.id.tv_producto_desc);
        tvOferta        = findViewById(R.id.tv_oferta);
        tvComisionLabel = findViewById(R.id.tv_comision_label);
        tvComision      = findViewById(R.id.tv_comision);
        rowCostoEnvio   = findViewById(R.id.row_costo_envio);
        tvCostoEnvio    = findViewById(R.id.tv_costo_envio);
        tvTotal         = findViewById(R.id.tv_total);
        tvErrorMediosPago = findViewById(R.id.tv_error_medios_pago);
        spinnerMediosPago = findViewById(R.id.spinner_medios_pago);
    }

    private void configurarListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_pagar).setOnClickListener(v -> {
            int selectedPosition = spinnerMediosPago.getSelectedItemPosition();
            if (mediosPagoIds == null || mediosPagoIds.isEmpty() || selectedPosition < 0) {
                Toast.makeText(this, "Debes seleccionar un medio de pago válido", Toast.LENGTH_SHORT).show();
                return;
            }
            Long medioPagoId = mediosPagoIds.get(selectedPosition);
            pagarItem(medioPagoId);
        });
    }

    private void cargarDatos() {
        tvProductoDesc.setText(descripcion != null ? descripcion : "Ítem #" + itemId);
        tvOferta.setText(String.format("$ %,.2f", oferta));

        double comisionMostrar = comision > 0 ? comision : oferta * 0.10;
        tvComisionLabel.setText(comision > 0 ? "Comisión de la casa" : "Comisión de la casa (est. 10%)");
        tvComision.setText(String.format("$ %,.2f", comisionMostrar));

        if (costoEnvio > 0) {
            rowCostoEnvio.setVisibility(View.VISIBLE);
            tvCostoEnvio.setText(String.format("$ %,.2f", costoEnvio));
        } else {
            rowCostoEnvio.setVisibility(View.GONE);
        }

        double total = totalAPagar > 0 ? totalAPagar : oferta + comisionMostrar + costoEnvio;
        tvTotal.setText(String.format("$ %,.2f", total));
    }

    /** Carga el item para obtener la moneda de la subasta y luego muestra los medios compatibles. */
    private void cargarMonedaYMediosPago() {
        RetrofitClient.getApiService().getItemDetalle(itemId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object monedaObj = response.body().get("moneda");
                    if (monedaObj instanceof String) {
                        subastaMoneda = (String) monedaObj;
                    }
                }
                cargarMediosPagoFiltrados();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                cargarMediosPagoFiltrados();
            }
        });
    }

    private void cargarMediosPagoFiltrados() {
        RetrofitClient.getApiService().getMediosPago().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> todos = response.body();
                    mediosPagoIds = new ArrayList<>();
                    List<String> mediosNombres = new ArrayList<>();

                    for (Map<String, Object> mp : todos) {
                        // Solo verificados
                        if (!Boolean.TRUE.equals(mp.get("verificado"))) continue;

                        Long id = null;
                        if (mp.get("id") instanceof Number) {
                            id = ((Number) mp.get("id")).longValue();
                        } else if (mp.get("identificador") instanceof Number) {
                            id = ((Number) mp.get("identificador")).longValue();
                        }
                        if (id == null) continue;

                        String tipoBE = (String) mp.get("tipo");
                        String monedaMP = (String) mp.get("moneda");
                        String detalle = (String) mp.get("detalle");

                        // Reglas de compatibilidad de moneda:
                        // - TARJETA_CREDITO: sirve para ARS y USD
                        // - CUENTA_BANCARIA: CBU → ARS, IBAN → USD (campo moneda del mp)
                        // - CHEQUE_CERTIFICADO: según moneda del cheque
                        boolean compatible;
                        if ("TARJETA_CREDITO".equals(tipoBE)) {
                            compatible = true;
                        } else {
                            compatible = subastaMoneda.equalsIgnoreCase(monedaMP != null ? monedaMP : "ARS");
                        }

                        if (!compatible) continue;

                        String tipoLabel;
                        if ("TARJETA_CREDITO".equals(tipoBE)) tipoLabel = "Tarjeta de crédito";
                        else if ("CUENTA_BANCARIA".equals(tipoBE)) tipoLabel = "Cuenta bancaria";
                        else tipoLabel = "Cheque certificado";

                        String nombre = tipoLabel;
                        if (detalle != null && !detalle.isEmpty()) nombre += " - " + detalle;
                        if (monedaMP != null) nombre += " (" + monedaMP + ")";

                        mediosNombres.add(nombre);
                        mediosPagoIds.add(id);
                    }

                    if (mediosNombres.isEmpty()) {
                        if (tvErrorMediosPago != null) {
                            tvErrorMediosPago.setVisibility(View.VISIBLE);
                            tvErrorMediosPago.setText("No tenés medios de pago verificados y compatibles con subastas en " + subastaMoneda + ".");
                        }
                        findViewById(R.id.btn_pagar).setEnabled(false);
                        findViewById(R.id.btn_pagar).setAlpha(0.5f);
                    } else {
                        if (tvErrorMediosPago != null) tvErrorMediosPago.setVisibility(View.GONE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ConfirmarPagoActivity.this,
                                android.R.layout.simple_spinner_item, mediosNombres);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMediosPago.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ConfirmarPagoActivity.this, "Error al cargar medios de pago", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pagarItem(Long medioPagoId) {
        findViewById(R.id.btn_pagar).setEnabled(false);
        findViewById(R.id.btn_pagar).setAlpha(0.5f);

        Map<String, Object> req = new HashMap<>();
        req.put("medioPagoId", medioPagoId);

        RetrofitClient.getApiService().pagarItemGanado(itemId, req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ConfirmarPagoActivity.this, "¡Pago procesado exitosamente!", Toast.LENGTH_LONG).show();
                    android.content.Intent intent = new android.content.Intent(ConfirmarPagoActivity.this, tpo.g16.blackwood.main.HomeActivity.class);
                    intent.putExtra("TAB_INDEX", 1);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Error al procesar el pago";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (body.contains("no puede operar") || body.contains("moneda")) {
                                errorMsg = "Este medio de pago no es compatible con la moneda de la subasta";
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(ConfirmarPagoActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    findViewById(R.id.btn_pagar).setEnabled(true);
                    findViewById(R.id.btn_pagar).setAlpha(1.0f);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ConfirmarPagoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                findViewById(R.id.btn_pagar).setEnabled(true);
                findViewById(R.id.btn_pagar).setAlpha(1.0f);
            }
        });
    }
}
