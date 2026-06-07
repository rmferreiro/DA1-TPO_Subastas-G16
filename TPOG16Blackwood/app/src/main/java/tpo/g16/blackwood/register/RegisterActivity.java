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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etPais, etDomicilio, etDocumento;
    private TextView tvErrorN, tvErrorA, tvErrorP, tvErrorD, tvErrorDoc;
    private Button   btnFrente, btnDorso, btnEnviar;

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
                                btnFrente.setAlpha(1f);
                            } else {
                                photoDorsoUri = Uri.fromFile(currentPhotoFile);
                                btnDorso.setText("✓ " + getString(R.string.dorso_capturado));
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
        etNombre    = findViewById(R.id.field_n);
        etApellido  = findViewById(R.id.field_a);
        etPais      = findViewById(R.id.field_p);
        etDomicilio = findViewById(R.id.field_d);
        etDocumento = findViewById(R.id.field_doc);
        
        // Error Labels
        tvErrorN   = findViewById(R.id.error_n);
        tvErrorA   = findViewById(R.id.error_a);
        tvErrorP   = findViewById(R.id.error_p);
        tvErrorD   = findViewById(R.id.error_d);
        tvErrorDoc = findViewById(R.id.error_doc);

        btnFrente   = findViewById(R.id.btn_frente);
        btnDorso    = findViewById(R.id.btn_dorso);
        btnEnviar   = findViewById(R.id.btn_enviar);

        // Hints por código
        etNombre.setHint(getString(R.string.nombre_ph));
        etApellido.setHint(getString(R.string.apellido_ph));
        etPais.setHint(getString(R.string.pais_ph));
        etDomicilio.setHint(getString(R.string.dom_ph));
        etDocumento.setHint(getString(R.string.doc_ph));

        btnEnviar.setAlpha(0.5f);
        btnEnviar.setEnabled(false);

        btnFrente.setOnClickListener(v -> { capturandoFrente = true; pedirCamaraOAbrir(); });
        btnDorso.setOnClickListener(v -> { capturandoFrente = false; pedirCamaraOAbrir(); });
        btnEnviar.setOnClickListener(v -> { if (validarCampos()) navegarAlSiguientePaso(); });
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
        // Resetear errores
        tvErrorN.setVisibility(View.GONE);
        tvErrorA.setVisibility(View.GONE);
        tvErrorP.setVisibility(View.GONE);
        tvErrorD.setVisibility(View.GONE);
        tvErrorDoc.setVisibility(View.GONE);

        boolean esValido = true;

        // Validar Nombre
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            mostrarError(tvErrorN, getString(R.string.error_nombre_obligatorio));
            esValido = false;
        } else if (nombre.length() < 3) {
            mostrarError(tvErrorN, getString(R.string.error_nombre_minimo));
            esValido = false;
        }

        // Validar Apellido
        String apellido = etApellido.getText().toString().trim();
        if (apellido.isEmpty()) {
            mostrarError(tvErrorA, getString(R.string.error_apellido_obligatorio));
            esValido = false;
        } else if (apellido.length() < 3) {
            mostrarError(tvErrorA, getString(R.string.error_apellido_minimo));
            esValido = false;
        }

        // Validar País
        String pais = etPais.getText().toString().trim();
        if (pais.isEmpty()) {
            mostrarError(tvErrorP, getString(R.string.error_pais_obligatorio));
            esValido = false;
        } else if (pais.length() < 3) {
            mostrarError(tvErrorP, getString(R.string.error_pais_minimo));
            esValido = false;
        }

        // Validar Domicilio
        if (etDomicilio.getText().toString().trim().isEmpty()) {
            mostrarError(tvErrorD, getString(R.string.error_domicilio_obligatorio));
            esValido = false;
        }

        // Validar Documento
        String doc = etDocumento.getText().toString().trim();
        if (doc.isEmpty()) {
            mostrarError(tvErrorDoc, getString(R.string.error_doc_obligatorio));
            esValido = false;
        } else if (!doc.matches("\\d+")) {
            mostrarError(tvErrorDoc, getString(R.string.error_doc_numeros));
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
}
