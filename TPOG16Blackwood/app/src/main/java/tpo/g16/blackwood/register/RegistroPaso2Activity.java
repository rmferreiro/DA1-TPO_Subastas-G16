package tpo.g16.blackwood.register;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.common.LoadingActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import tpo.g16.blackwood.network.ApiConfig;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.model.ApiError;
import tpo.g16.blackwood.network.model.AuthResponse;
import tpo.g16.blackwood.network.model.MedioPagoRequest;
import tpo.g16.blackwood.network.model.RegistroRequest;
import com.google.gson.Gson;

public class RegistroPaso2Activity extends AppCompatActivity {

    private Button btnAgregarMetodo, btnFinalizar;
    private EditText etPassword, etConfirmPassword;
    private TextView tvErrorPassword, tvErrorConfirmPassword;
    private ScrollView mainContent;
    private FrameLayout fragmentContainer;
    private LinearLayout containerMetodosPago;

    private List<MedioPagoRequest> mediosPagoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_paso2);

        // Referencias
        btnAgregarMetodo = findViewById(R.id.btn_agregar_metodo);
        btnFinalizar = findViewById(R.id.btn_finalizar);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        
        // Error labels
        tvErrorPassword = findViewById(R.id.error_password);
        tvErrorConfirmPassword = findViewById(R.id.error_confirm_password);

        mainContent = findViewById(R.id.main_registration_content);
        fragmentContainer = findViewById(R.id.fragment_container);
        containerMetodosPago = findViewById(R.id.container_metodos_pago);

        // Listener para el botón final
        btnFinalizar.setOnClickListener(v -> {
            if (validarSeguridad()) {
                realizarRegistroBackend();
            }
        });

        // Validación visual en tiempo real (opcional, para feedback de coincidencia)
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validarCoincidenciaSilenciosa();
            }
        };
        etPassword.addTextChangedListener(passwordWatcher);
        etConfirmPassword.addTextChangedListener(passwordWatcher);

        // Listener para abrir el fragmento (Creación)
        btnAgregarMetodo.setOnClickListener(v -> abrirFragmentoPago(null, null, -1));

        // Listener para recibir los datos del fragmento
        getSupportFragmentManager().setFragmentResultListener("add_payment_request", this, (requestKey, bundle) -> {
            String tipo = bundle.getString("tipo");
            String detalle = bundle.getString("detalle");
            int editIndex = bundle.getInt("edit_index", -1);

            if (editIndex != -1) {
                actualizarTarjeta(editIndex, tipo, detalle);
            } else {
                agregarNuevaTarjeta(tipo, detalle);
            }
        });

        // Controlar visibilidad al volver atrás
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                mainContent.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
            }
        });
    }

    private boolean validarSeguridad() {
        // Resetear visibilidad de errores
        tvErrorPassword.setVisibility(View.GONE);
        tvErrorConfirmPassword.setVisibility(View.GONE);

        String pass = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();
        boolean esValido = true;

        if (pass.isEmpty()) {
            mostrarError(tvErrorPassword, getString(R.string.error_password_obligatorio));
            esValido = false;
        } else if (pass.length() < 6) {
            mostrarError(tvErrorPassword, getString(R.string.error_password_minimo));
            esValido = false;
        }

        if (confirm.isEmpty()) {
            mostrarError(tvErrorConfirmPassword, getString(R.string.error_confirm_password_obligatorio));
            esValido = false;
        } else if (!pass.equals(confirm)) {
            mostrarError(tvErrorConfirmPassword, getString(R.string.error_passwords_no_coinciden));
            esValido = false;
        }

        return esValido;
    }

    private void mostrarError(TextView tv, String mensaje) {
        tv.setText(mensaje);
        tv.setVisibility(View.VISIBLE);
    }

    private void validarCoincidenciaSilenciosa() {
        String pass = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();
        
        // Si ya hay texto en ambos, damos feedback visual con el color Gold
        if (!confirm.isEmpty() && !pass.equals(confirm)) {
            etConfirmPassword.setTextColor(ContextCompat.getColor(this, R.color.gold)); // Gold
        } else {
            etConfirmPassword.setTextColor(ContextCompat.getColor(this, R.color.charcoal)); // Original
            tvErrorConfirmPassword.setVisibility(View.GONE);
        }
    }

    private void navegarACompletado() {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra(LoadingActivity.EXTRA_TITLE, getString(R.string.loading_finalizando_registro));
        intent.putExtra(LoadingActivity.EXTRA_DESC, getString(R.string.loading_creando_perfil));
        intent.putExtra(LoadingActivity.EXTRA_NEXT_ACTIVITY, RegistroCompletadoActivity.class.getName());
        startActivity(intent);
        finish();
    }

    private void realizarRegistroBackend() {
        btnFinalizar.setEnabled(false);
        btnFinalizar.setText("Completando registro...");

        SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString(ApiConfig.KEY_REGISTRATION_EMAIL, null);
        String password = etPassword.getText().toString();

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Error de inconsistencia: Email no encontrado en preferencias.", Toast.LENGTH_LONG).show();
            btnFinalizar.setEnabled(true);
            btnFinalizar.setText(R.string.final_reg);
            return;
        }

        tpo.g16.blackwood.network.model.CompletarRegistroRequest request = 
                new tpo.g16.blackwood.network.model.CompletarRegistroRequest(email, password, mediosPagoList);

        RetrofitClient.getInstance(this).getAuthApiService()
                .completarRegistro(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Limpiar las llaves de estado de registro
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove(ApiConfig.KEY_REGISTRATION_STATE);
                            editor.remove(ApiConfig.KEY_REGISTRATION_EMAIL);
                            editor.remove(ApiConfig.KEY_REGISTRATION_NOMBRE);
                            editor.remove(ApiConfig.KEY_REGISTRATION_APELLIDO);
                            editor.apply();

                            // Mostrar Toast largo de éxito solicitado
                            Toast.makeText(RegistroPaso2Activity.this, 
                                    "Usuario creado con éxito, usa tus credenciales para hacer login", 
                                    Toast.LENGTH_LONG).show();

                            // Redirigir a la pantalla principal de Login limpiando la pila
                            Intent intent = new Intent(RegistroPaso2Activity.this, tpo.g16.blackwood.login.LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            btnFinalizar.setEnabled(true);
                            btnFinalizar.setText(R.string.final_reg);
                            try {
                                if (response.errorBody() != null) {
                                    ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
                                    Toast.makeText(RegistroPaso2Activity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(RegistroPaso2Activity.this, "Error al completar el registro", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnFinalizar.setEnabled(true);
                        btnFinalizar.setText(R.string.final_reg);
                        Toast.makeText(RegistroPaso2Activity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return "";
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void abrirFragmentoPago(String tipo, String detalle, int index) {
        mainContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        AddPaymentMethodFragment fragment = new AddPaymentMethodFragment();
        if (tipo != null) {
            Bundle args = new Bundle();
            args.putString("edit_tipo", tipo);
            args.putString("edit_detalle", detalle);
            args.putInt("edit_index", index);
            fragment.setArguments(args);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void agregarNuevaTarjeta(String tipo, String detalle) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_metodo_pago, containerMetodosPago, false);
        configurarCard(cardView, tipo, detalle);
        containerMetodosPago.addView(cardView);
        guardarMedioPagoRequest(tipo, detalle);
    }

    private void actualizarTarjeta(int index, String tipo, String detalle) {
        View cardView = containerMetodosPago.getChildAt(index);
        if (cardView != null) {
            configurarCard(cardView, tipo, detalle);
            if (index < mediosPagoList.size()) {
                mediosPagoList.set(index, crearMedioPagoRequest(tipo, detalle));
            }
        }
    }

    private void configurarCard(View cardView, String tipo, String detalle) {
        TextView tvTitulo = cardView.findViewById(R.id.tv_metodo_titulo);
        TextView tvDetalle = cardView.findViewById(R.id.tv_metodo_detalle);
        View btnEditar = cardView.findViewById(R.id.btn_editar_metodo);
        View btnEliminar = cardView.findViewById(R.id.btn_eliminar_metodo);

        tvTitulo.setText(tipo);
        tvDetalle.setText(detalle);

        btnEditar.setOnClickListener(v -> {
            int index = containerMetodosPago.indexOfChild(cardView);
            abrirFragmentoPago(tipo, detalle, index);
        });

        btnEliminar.setOnClickListener(v -> {
            int index = containerMetodosPago.indexOfChild(cardView);
            if (index != -1 && index < mediosPagoList.size()) {
                mediosPagoList.remove(index);
            }
            containerMetodosPago.removeView(cardView);
        });
    }

    private void guardarMedioPagoRequest(String tipoString, String detalle) {
        MedioPagoRequest req = crearMedioPagoRequest(tipoString, detalle);
        mediosPagoList.add(req);
    }

    private MedioPagoRequest crearMedioPagoRequest(String tipoString, String detalle) {
        MedioPagoRequest req = new MedioPagoRequest();
        req.setMoneda("ARS");
        
        String[] parts = detalle.split("; ");
        
        if (tipoString.toLowerCase().contains("tarjeta")) {
            req.setTipo("TARJETA_CREDITO");
            if (parts.length > 0) req.setNumeroTarjeta(parts[0]);
            if (parts.length > 1) req.setTitular(parts[1]);
            if (parts.length > 2) req.setVencimiento(parts[2]);
            req.setEsTarjetaInternacional(false);
        } else if (tipoString.toLowerCase().contains("cuenta")) {
            req.setTipo("CUENTA_BANCARIA");
            if (parts.length > 0) req.setBanco(parts[0]);
            if (parts.length > 1) req.setNumeroCuenta(parts[1]);
            if (parts.length > 2) req.setCbuSwift(parts[2]);
            req.setEsInternacional(false);
        } else {
            req.setTipo("CHEQUE_CERTIFICADO");
            if (parts.length > 0) req.setBancoEmisor(parts[0]);
            if (parts.length > 1) req.setNumeroCheque(parts[1]);
            req.setMontoCertificado(new java.math.BigDecimal("0"));
        }
        return req;
    }
}
