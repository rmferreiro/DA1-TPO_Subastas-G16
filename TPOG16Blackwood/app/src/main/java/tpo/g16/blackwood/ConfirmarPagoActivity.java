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
    private String descripcion = "";

    private TextView tvProductoDesc, tvOferta, tvComision, tvTotal, tvErrorMediosPago;
    private Spinner spinnerMediosPago;
    private List<Map<String, Object>> mediosPagoList;
    private List<Long> mediosPagoIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_pago);

        itemId = getIntent().getIntExtra("ITEM_ID", -1);
        oferta = getIntent().getDoubleExtra("OFERTA", 0.0);
        descripcion = getIntent().getStringExtra("DESCRIPCION");

        initViews();
        configurarListeners();
        
        if (itemId != -1) {
            cargarDatos();
            cargarMediosPago();
        } else {
            Toast.makeText(this, "Error: Item no especificado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvProductoDesc = findViewById(R.id.tv_producto_desc);
        tvOferta = findViewById(R.id.tv_oferta);
        tvComision = findViewById(R.id.tv_comision);
        tvTotal = findViewById(R.id.tv_total);
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
        tvOferta.setText(String.format("$ %.2f", oferta));
        
        double comision = oferta * 0.10; // Asumiendo 10%
        tvComision.setText(String.format("$ %.2f", comision));
        
        double total = oferta + comision;
        tvTotal.setText(String.format("$ %.2f", total));
    }

    private void cargarMediosPago() {
        RetrofitClient.getApiService().getMediosPago().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mediosPagoList = response.body();
                    mediosPagoIds = new ArrayList<>();
                    List<String> mediosNombres = new ArrayList<>();

                    for (Map<String, Object> mp : mediosPagoList) {
                        Long id = ((Number) mp.get("identificador")).longValue();
                        String tipo = (String) mp.get("tipo");
                        String red = (String) mp.get("red");
                        String num = (String) mp.get("numeroTarjeta");
                        
                        String nombre = tipo + " " + red + " terminada en " + num.substring(Math.max(0, num.length() - 4));
                        
                        mediosNombres.add(nombre);
                        mediosPagoIds.add(id);
                    }

                    if (mediosNombres.isEmpty()) {
                        tvErrorMediosPago.setVisibility(View.VISIBLE);
                        findViewById(R.id.btn_pagar).setEnabled(false);
                        findViewById(R.id.btn_pagar).setAlpha(0.5f);
                    } else {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ConfirmarPagoActivity.this, android.R.layout.simple_spinner_item, mediosNombres);
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
                    android.content.Intent intent = new android.content.Intent(ConfirmarPagoActivity.this, HomeActivity.class);
                    intent.putExtra("TAB_INDEX", 1);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    findViewById(R.id.btn_pagar).setEnabled(true);
                    findViewById(R.id.btn_pagar).setAlpha(1.0f);
                    Toast.makeText(ConfirmarPagoActivity.this, "Error al procesar el pago", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                findViewById(R.id.btn_pagar).setEnabled(true);
                findViewById(R.id.btn_pagar).setAlpha(1.0f);
                Toast.makeText(ConfirmarPagoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
