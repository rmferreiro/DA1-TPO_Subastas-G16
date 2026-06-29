package tpo.g16.blackwood.register;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.R;
import tpo.g16.blackwood.network.RetrofitClient;

public class SeguimientoLoteActivity extends AppCompatActivity {

    private TextView txtTitulo, txtSubtitulo, txtUbicacion;
    private View stepDot2, stepDot3;
    private TextView txtStep3Label;
    private int productoId;

    // Nuevas Vistas Dinámicas
    private View cardSeguro, cardUbicacion, cardMotivosRechazo, cardPropuestaDuenio;
    private TextView txtMotivosRechazo, txtMontoPropuesto;
    private View btnPropuestaAceptar, btnPropuestaRechazar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_lote);

        txtTitulo = findViewById(R.id.txt_titulo_lote);
        txtSubtitulo = findViewById(R.id.txt_subtitulo_lote);
        txtUbicacion = findViewById(R.id.txt_ubicacion_lote);
        stepDot2 = findViewById(R.id.step_dot_2);
        stepDot3 = findViewById(R.id.step_dot_3);
        txtStep3Label = findViewById(R.id.txt_step_3_label);

        // Vincular nuevas vistas
        cardSeguro = findViewById(R.id.card_seguro);
        cardUbicacion = findViewById(R.id.card_ubicacion);
        cardMotivosRechazo = findViewById(R.id.card_motivos_rechazo);
        cardPropuestaDuenio = findViewById(R.id.card_propuesta_duenio);

        txtMotivosRechazo = findViewById(R.id.txt_motivos_rechazo);
        txtMontoPropuesto = findViewById(R.id.txt_monto_propuesto);
        btnPropuestaAceptar = findViewById(R.id.btn_propuesta_aceptar);
        btnPropuestaRechazar = findViewById(R.id.btn_propuesta_rechazar);

        productoId = getIntent().getIntExtra("productoId", 0);

        TextView headerSubtitle = findViewById(R.id.header_subtitle);
        if (headerSubtitle != null) {
            headerSubtitle.setText("Lote pendiente");
        }

        findViewById(R.id.btn_contactar_soporte).setOnClickListener(v -> 
                Toast.makeText(this, "No disponible en este momento, intenta más tarde", Toast.LENGTH_SHORT).show());

        btnPropuestaAceptar.setOnClickListener(v -> responderPropuesta("ACEPTADO"));
        btnPropuestaRechazar.setOnClickListener(v -> responderPropuesta("RECHAZADO"));

        obtenerDetalles();
    }

    private void obtenerDetalles() {
        RetrofitClient.getApiService()
                .getProductoDetalle(productoId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> data = response.body();
                            
                            String descripcion = (String) data.get("descripcion");
                            String subtitulo = (String) data.get("subtitulo");
                            String ubicacion = (String) data.get("ubicacionDeposito");
                            String estado = (String) data.get("estado");
                            String motivoRechazo = (String) data.get("motivoRechazo");

                            txtTitulo.setText(descripcion != null ? descripcion : "Lote sin título");
                            txtSubtitulo.setText(subtitulo != null ? subtitulo : "Sin subtítulo");
                                                     if ("PENDIENTE".equals(estado)) {
                                txtUbicacion.setText("En posesion del usuario, preparando envío hacía depósito central en caso de aprobación.");
                            } else if (ubicacion != null && !ubicacion.trim().isEmpty()) {
                                txtUbicacion.setText(ubicacion);
                            } else {
                                txtUbicacion.setText("Depósito central · Buenos Aires");
                            }

                            // Configurar visibilidad de tarjetas según estado
                            if ("PENDIENTE".equals(estado)) {
                                cardSeguro.setVisibility(View.VISIBLE);
                                cardUbicacion.setVisibility(View.VISIBLE);
                                cardMotivosRechazo.setVisibility(View.GONE);
                                cardPropuestaDuenio.setVisibility(View.GONE);
                            } else if ("RECHAZADO_DUENIO".equals(estado)) {
                                cardSeguro.setVisibility(View.GONE);
                                cardUbicacion.setVisibility(View.GONE);
                                cardMotivosRechazo.setVisibility(View.GONE);
                                cardPropuestaDuenio.setVisibility(View.GONE);
                            } else if ("RECHAZADO_EMPRESA".equals(estado) || "RECHAZADO".equals(estado)) {
                                cardSeguro.setVisibility(View.GONE);
                                cardUbicacion.setVisibility(View.GONE);
                                cardPropuestaDuenio.setVisibility(View.GONE);
                                cardMotivosRechazo.setVisibility(View.VISIBLE);
                                txtMotivosRechazo.setText(motivoRechazo != null && !motivoRechazo.trim().isEmpty() ? motivoRechazo : "No especificado");
                            } else if ("PENDIENTE_DUENIO".equals(estado)) {
                                cardSeguro.setVisibility(View.VISIBLE);
                                cardUbicacion.setVisibility(View.VISIBLE);
                                cardMotivosRechazo.setVisibility(View.GONE);
                                cardPropuestaDuenio.setVisibility(View.VISIBLE);

                                Double precioBase = (Double) data.get("precioBasePropuesto");
                                String moneda = (String) data.get("moneda");
                                Double comisionProp = (Double) data.get("comisionPropuesta");
                                
                                int comisionPct = 5; // Fallback por defecto 5%
                                if (comisionProp != null) {
                                    comisionPct = (int) Math.round(comisionProp * 100);
                                }
                                String textoPropuesta = (moneda != null ? moneda : "ARS") + " " + String.format("%.2f", precioBase != null ? precioBase : 0.0)
                                        + " (Comisión: " + comisionPct + "%)";
                                txtMontoPropuesto.setText(textoPropuesta);
                            } else if ("ACEPTADO_DUENIO".equals(estado) || "SUBASTANDO".equals(estado) || "EN_SUBASTA".equals(estado)) {
                                cardSeguro.setVisibility(View.VISIBLE);
                                cardUbicacion.setVisibility(View.VISIBLE);
                                cardMotivosRechazo.setVisibility(View.GONE);
                                cardPropuestaDuenio.setVisibility(View.GONE);
                            }

                            // Configurar Stepper
                            actualizarStepper(estado);
                        } else {
                            Toast.makeText(SeguimientoLoteActivity.this, "Error al obtener detalles del lote", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(SeguimientoLoteActivity.this, "Falla al conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void responderPropuesta(String decision) {
        btnPropuestaAceptar.setEnabled(false);
        btnPropuestaRechazar.setEnabled(false);

        Map<String, Object> request = new HashMap<>();
        request.put("decision", decision);

        RetrofitClient.getApiService().responderCondiciones(productoId, request)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        btnPropuestaAceptar.setEnabled(true);
                        btnPropuestaRechazar.setEnabled(true);
                        if (response.isSuccessful()) {
                            if ("ACEPTADO".equals(decision)) {
                                Toast.makeText(SeguimientoLoteActivity.this, "Propuesta aceptada", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SeguimientoLoteActivity.this, "Propuesta rechazada", Toast.LENGTH_SHORT).show();
                            }
                            obtenerDetalles();
                        } else {
                            Toast.makeText(SeguimientoLoteActivity.this, "Error al responder propuesta", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnPropuestaAceptar.setEnabled(true);
                        btnPropuestaRechazar.setEnabled(true);
                        Toast.makeText(SeguimientoLoteActivity.this, "Falla al conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarStepper(String estado) {
        // Por defecto: paso 1 siempre completo (dorado)
        // Paso 2: "En inspección"
        if ("PENDIENTE".equals(estado)) {
            // El lote está en inspección inicial en la empresa
            stepDot2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            
            // Paso 3 permanece gris e inactivo
            stepDot3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray)));
            txtStep3Label.setTextColor(getResources().getColor(android.R.color.darker_gray));
            txtStep3Label.setText("Pendiente decisión");
        } else if ("PENDIENTE_DUENIO".equals(estado)) {
            // Lote aprobado por la administración, esperando firma del dueño
            stepDot2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            
            stepDot3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            txtStep3Label.setTextColor(getResources().getColor(R.color.gold));
            txtStep3Label.setText("Esperando respuesta del dueño");
        } else if ("ACEPTADO_DUENIO".equals(estado) || "ACEPTADO".equals(estado)) {
            // Decisión aceptada final por el dueño
            stepDot2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            
            stepDot3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            txtStep3Label.setTextColor(getResources().getColor(R.color.gold));
            txtStep3Label.setText("Aceptado para subasta");
        } else if ("SUBASTANDO".equals(estado) || "EN_SUBASTA".equals(estado)) {
            stepDot2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            
            stepDot3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            txtStep3Label.setTextColor(getResources().getColor(R.color.gold));
            txtStep3Label.setText("En subasta");
        } else if ("RECHAZADO_EMPRESA".equals(estado) || "RECHAZADO_DUENIO".equals(estado) || "RECHAZADO".equals(estado)) {
            // Decisión tomada: Rechazado
            stepDot2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
            
            stepDot3.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Rojo
            txtStep3Label.setTextColor(Color.parseColor("#F44336"));
            txtStep3Label.setText("Rechazado");
        }
    }
}
