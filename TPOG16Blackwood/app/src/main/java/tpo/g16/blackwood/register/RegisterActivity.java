package tpo.g16.blackwood.register;

import tpo.g16.blackwood.R;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Base64;
import android.content.SharedPreferences;
import android.content.Context;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.ApiConfig;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.model.ApiError;
import tpo.g16.blackwood.network.model.AuthResponse;
import tpo.g16.blackwood.network.model.RegistroRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etNombre, etApellido, etDomicilio, etDocumento;
    private AutoCompleteTextView etPais;
    private TextView tvErrorEmail, tvErrorN, tvErrorA, tvErrorP, tvErrorD, tvErrorDoc;
    private MaterialButton   btnFrente, btnDorso, btnEnviar;

    private Uri  photoFrenteUri = null;
    private Uri  photoDorsoUri  = null;
    private File currentPhotoFile;

    private boolean capturandoFrente = true;

    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            abrirCamara();
                        } else {
                            Toast.makeText(this, getString(R.string.error_camara_permiso), Toast.LENGTH_LONG).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Uri> camaraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success) {
                            if (capturandoFrente) {
                                photoFrenteUri = Uri.fromFile(currentPhotoFile);
                                btnFrente.setText("✓ " + getString(R.string.frente_capturado));
                                btnFrente.setIcon(null);
                                btnFrente.setAlpha(1f);
                            } else {
                                photoDorsoUri = Uri.fromFile(currentPhotoFile);
                                btnDorso.setText("✓ " + getString(R.string.dorso_capturado));
                                btnDorso.setIcon(null);
                                btnDorso.setAlpha(1f);
                            }
                            verificarBotónEnviar();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- DESACTIVACIÓN AGRESIVA DEL SERVICIO ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AutofillManager afm = getSystemService(AutofillManager.class);
            if (afm != null) {
                afm.cancel();
                afm.disableAutofillServices();
            }
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }

        // Inputs
        etEmail     = findViewById(R.id.field_email);
        etNombre    = findViewById(R.id.field_n);
        etApellido  = findViewById(R.id.field_a);
        etPais      = findViewById(R.id.field_p);
        etDomicilio = findViewById(R.id.field_d);
        etDocumento = findViewById(R.id.field_doc);
        
        // Error Labels
        tvErrorEmail = findViewById(R.id.error_email);
        tvErrorN   = findViewById(R.id.error_n);
        tvErrorA   = findViewById(R.id.error_a);
        tvErrorP   = findViewById(R.id.error_p);
        tvErrorD   = findViewById(R.id.error_d);
        tvErrorDoc = findViewById(R.id.error_doc);

        btnFrente   = findViewById(R.id.btn_frente);
        btnDorso    = findViewById(R.id.btn_dorso);
        btnEnviar   = findViewById(R.id.btn_enviar);

        // Hints por código
        etEmail.setHint(getString(R.string.login_email_label));
        etNombre.setHint(getString(R.string.nombre_ph));
        etApellido.setHint(getString(R.string.apellido_ph));
        etPais.setHint(getString(R.string.pais_ph));
        etDomicilio.setHint(getString(R.string.dom_ph));
        etDocumento.setHint(getString(R.string.doc_ph));

        // Configurar dropdown de países
        String[] paises = {
                getString(R.string.pais_argentina),
                getString(R.string.pais_brasil),
                getString(R.string.pais_uruguay),
                getString(R.string.pais_chile),
                getString(R.string.pais_paraguay),
                getString(R.string.pais_bolivia),
                getString(R.string.pais_peru),
                getString(R.string.pais_colombia),
                getString(R.string.pais_mexico),
                getString(R.string.pais_espana),
                getString(R.string.pais_estados_unidos),
                getString(R.string.pais_alemania),
                getString(R.string.pais_francia),
                getString(R.string.pais_italia),
                getString(R.string.pais_reino_unido)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, paises);
        etPais.setAdapter(adapter);
        etPais.setOnItemClickListener((parent, view, position, id) -> {
            tvErrorP.setVisibility(View.GONE);
            etPais.setBackgroundResource(R.drawable.input_bg);
        });

        // Limpiar errores del email al escribir
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tvErrorEmail.setVisibility(View.GONE);
                etEmail.setBackgroundResource(R.drawable.input_bg);
            }
        });

        // Limpiar errores del documento al escribir
        etDocumento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tvErrorDoc.setVisibility(View.GONE);
                etDocumento.setBackgroundResource(R.drawable.input_bg);
            }
        });

        btnEnviar.setAlpha(0.5f);
        btnEnviar.setEnabled(false);

        btnFrente.setOnClickListener(v -> { capturandoFrente = true; pedirCamaraOAbrir(); });
        btnDorso.setOnClickListener(v -> { capturandoFrente = false; pedirCamaraOAbrir(); });
        btnEnviar.setOnClickListener(v -> { if (validarCampos()) enviarRegistro(); });
    }

    private void pedirCamaraOAbrir() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        try {
            currentPhotoFile = crearArchivoFoto();
            Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", currentPhotoFile);
            camaraLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_camara_preparar), Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoFoto() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nombre = "DNI_" + (capturandoFrente ? "FRENTE" : "DORSO") + "_" + timestamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombre, ".jpg", storageDir);
    }

    private boolean validarCampos() {
        // Resetear errores y fondos
        tvErrorEmail.setVisibility(View.GONE);
        tvErrorN.setVisibility(View.GONE);
        tvErrorA.setVisibility(View.GONE);
        tvErrorP.setVisibility(View.GONE);
        tvErrorD.setVisibility(View.GONE);
        tvErrorDoc.setVisibility(View.GONE);

        etEmail.setBackgroundResource(R.drawable.input_bg);
        etNombre.setBackgroundResource(R.drawable.input_bg);
        etApellido.setBackgroundResource(R.drawable.input_bg);
        etPais.setBackgroundResource(R.drawable.input_bg);
        etDomicilio.setBackgroundResource(R.drawable.input_bg);
        etDocumento.setBackgroundResource(R.drawable.input_bg);

        boolean esValido = true;

        // Validar Email
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            mostrarError(tvErrorEmail, getString(R.string.error_email_invalido));
            etEmail.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarError(tvErrorEmail, getString(R.string.error_email_invalido));
            etEmail.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        }

        // Validar Nombre
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            mostrarError(tvErrorN, getString(R.string.error_nombre_obligatorio));
            etNombre.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        } else if (nombre.length() < 3) {
            mostrarError(tvErrorN, getString(R.string.error_nombre_minimo));
            etNombre.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        }

        // Validar Apellido
        String apellido = etApellido.getText().toString().trim();
        if (apellido.isEmpty()) {
            mostrarError(tvErrorA, getString(R.string.error_apellido_obligatorio));
            etApellido.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        } else if (apellido.length() < 3) {
            mostrarError(tvErrorA, getString(R.string.error_apellido_minimo));
            etApellido.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        }

        // Validar País
        String pais = etPais.getText().toString().trim();
        if (pais.isEmpty()) {
            mostrarError(tvErrorP, getString(R.string.error_pais_obligatorio));
            etPais.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        } else if (pais.length() < 3) {
            mostrarError(tvErrorP, getString(R.string.error_pais_minimo));
            etPais.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        }

        // Validar Domicilio
        if (etDomicilio.getText().toString().trim().isEmpty()) {
            mostrarError(tvErrorD, getString(R.string.error_domicilio_obligatorio));
            etDomicilio.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        }

        // Validar Documento
        String doc = etDocumento.getText().toString().trim();
        if (doc.isEmpty()) {
            mostrarError(tvErrorDoc, getString(R.string.error_doc_obligatorio));
            etDocumento.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        } else if (!doc.matches("\\d+")) {
            mostrarError(tvErrorDoc, getString(R.string.error_doc_numeros));
            etDocumento.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        } else if (doc.length() != 8) {
            mostrarError(tvErrorDoc, getString(R.string.error_doc_ocho_digitos));
            etDocumento.setBackgroundResource(R.drawable.input_bg_error);
            esValido = false;
        }

        if (!esValido) return false;

        if (photoFrenteUri == null || photoDorsoUri == null) {
            Toast.makeText(this, getString(R.string.error_fotos_faltantes), Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private void mostrarError(TextView tv, String mensaje) {
        tv.setText(mensaje);
        tv.setVisibility(View.VISIBLE);
    }

    private void verificarBotónEnviar() {
        if (photoFrenteUri != null && photoDorsoUri != null) {
            btnEnviar.setEnabled(true);
            btnEnviar.setAlpha(1f);
        }
    }

    private void navegarAlSiguientePaso() {
        // Directo a RegistroEnProcesoActivity para evitar la doble pantalla de verificación
        Intent intent = new Intent(this, RegistroEnProcesoActivity.class);
        intent.putExtra("nombre", etNombre.getText().toString().trim());
        intent.putExtra("apellido", etApellido.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    private void enviarRegistro() {
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Registrando...");

        String email = etEmail.getText().toString().trim();
        String nombreCompleto = etNombre.getText().toString().trim() + " " + etApellido.getText().toString().trim();
        String documento = etDocumento.getText().toString().trim();
        String direccion = etDomicilio.getText().toString().trim();
        Integer paisId = getPaisId(etPais.getText().toString().trim());
        
        String base64Frente = photoFrenteUri != null ? uriToBase64(photoFrenteUri) : "";
        String base64Dorso = photoDorsoUri != null ? uriToBase64(photoDorsoUri) : "";

        RegistroRequest request = new RegistroRequest(
                nombreCompleto, documento, direccion, paisId, email, "", base64Frente, base64Dorso
        );

        RetrofitClient.getInstance(this).getAuthApiService()
                .registrar(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putString(ApiConfig.KEY_REGISTRATION_STATE, "ESPERANDO_APROBACION")
                                    .putString(ApiConfig.KEY_REGISTRATION_EMAIL, email)
                                    .putString(ApiConfig.KEY_REGISTRATION_NOMBRE, etNombre.getText().toString().trim())
                                    .putString(ApiConfig.KEY_REGISTRATION_APELLIDO, etApellido.getText().toString().trim())
                                    .apply();

                            Intent intent = new Intent(RegisterActivity.this, RegistroEnProcesoActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            btnEnviar.setEnabled(true);
                            btnEnviar.setText("Enviar solicitud");
                            try {
                                if (response.errorBody() != null) {
                                    ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
                                    Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(RegisterActivity.this, "Error al registrar datos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnEnviar.setEnabled(true);
                        btnEnviar.setText("Enviar solicitud");
                        Toast.makeText(RegisterActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private Integer getPaisId(String nombre) {
        if (nombre == null) return 1;
        switch (nombre) {
            case "Argentina": return 1;
            case "Brasil": return 2;
            case "Uruguay": return 3;
            case "Chile": return 4;
            case "Paraguay": return 5;
            case "Bolivia": return 6;
            case "Perú": return 7;
            case "Colombia": return 8;
            case "México": return 9;
            case "España": return 10;
            case "Estados Unidos": return 11;
            case "Alemania": return 12;
            case "Francia": return 13;
            case "Italia": return 14;
            case "Reino Unido": return 15;
            default: return 1; // Argentina por defecto o no mapeado
        }
    }
}
