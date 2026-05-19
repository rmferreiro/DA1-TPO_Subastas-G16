package tpo.g16.blackwood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
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

    // ── Views ─────────────────────────────────────────────────────────────
    private EditText etNombre, etApellido, etPais, etDomicilio;
    private Button   btnFrente, btnDorso, btnEnviar;

    // ── Estado de fotos ────────────────────────────────────────────────────
    private Uri  photoFrenteUri = null;
    private Uri  photoDorsoUri  = null;
    private File currentPhotoFile;

    // ── Qué foto estamos tomando (true = frente, false = dorso) ───────────
    private boolean capturandoFrente = true;

    // ── Launchers ──────────────────────────────────────────────────────────

    // Permiso de cámara
    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            abrirCamara();
                        } else {
                            Toast.makeText(this,
                                    "Se necesita permiso de cámara para escanear el documento.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );

    // Resultado de la cámara
    private final ActivityResultLauncher<Uri> camaraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success) {
                            if (capturandoFrente) {
                                photoFrenteUri = Uri.fromFile(currentPhotoFile);
                                btnFrente.setText("✓ Frente capturado");
                                btnFrente.setAlpha(1f);
                            } else {
                                photoDorsoUri = Uri.fromFile(currentPhotoFile);
                                btnDorso.setText("✓ Dorso capturado");
                                btnDorso.setAlpha(1f);
                            }
                            verificarBotónEnviar();
                        } else {
                            Toast.makeText(this, "No se tomó la foto.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre    = findViewById(R.id.et_nombre);
        etApellido  = findViewById(R.id.et_apellido);
        etPais      = findViewById(R.id.et_pais);
        etDomicilio = findViewById(R.id.et_domicilio);
        btnFrente   = findViewById(R.id.btn_frente);
        btnDorso    = findViewById(R.id.btn_dorso);
        btnEnviar   = findViewById(R.id.btn_enviar);

        // Botón deshabilitado hasta que haya fotos + datos
        btnEnviar.setAlpha(0.5f);
        btnEnviar.setEnabled(false);

        btnFrente.setOnClickListener(v -> {
            capturandoFrente = true;
            pedirCamaraOAbrir();
        });

        btnDorso.setOnClickListener(v -> {
            capturandoFrente = false;
            pedirCamaraOAbrir();
        });

        btnEnviar.setOnClickListener(v -> {
            if (validarCampos()) {
                navegarAlSiguientePaso();
            }
        });
    }

    // ── Cámara ─────────────────────────────────────────────────────────────

    private void pedirCamaraOAbrir() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        try {
            currentPhotoFile = crearArchivoFoto();
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    currentPhotoFile
            );
            camaraLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error al preparar la cámara.", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoFoto() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String nombre = "DNI_" + (capturandoFrente ? "FRENTE" : "DORSO") + "_" + timestamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombre, ".jpg", storageDir);
    }

    // ── Validación ─────────────────────────────────────────────────────────

    private boolean validarCampos() {
        String nombre    = etNombre.getText().toString().trim();
        String apellido  = etApellido.getText().toString().trim();
        String pais      = etPais.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresá tu nombre");
            etNombre.requestFocus();
            return false;
        }
        if (apellido.isEmpty()) {
            etApellido.setError("Ingresá tu apellido");
            etApellido.requestFocus();
            return false;
        }
        if (pais.isEmpty()) {
            etPais.setError("Ingresá tu país");
            etPais.requestFocus();
            return false;
        }
        if (domicilio.isEmpty()) {
            etDomicilio.setError("Ingresá tu domicilio");
            etDomicilio.requestFocus();
            return false;
        }
        if (photoFrenteUri == null) {
            Toast.makeText(this, "Falta foto del frente del documento.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (photoDorsoUri == null) {
            Toast.makeText(this, "Falta foto del dorso del documento.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verificarBotónEnviar() {
        // Habilita el botón enviar cuando ambas fotos están capturadas
        if (photoFrenteUri != null && photoDorsoUri != null) {
            btnEnviar.setEnabled(true);
            btnEnviar.setAlpha(1f);
        }
    }

    // ── Navegación ─────────────────────────────────────────────────────────

    private void navegarAlSiguientePaso() {
        Intent intent = new Intent(this, RegistroEnProcesoActivity.class);

        // Pasamos los datos al siguiente paso
        intent.putExtra("nombre",    etNombre.getText().toString().trim());
        intent.putExtra("apellido",  etApellido.getText().toString().trim());
        intent.putExtra("pais",      etPais.getText().toString().trim());
        intent.putExtra("domicilio", etDomicilio.getText().toString().trim());
        intent.putExtra("foto_frente", photoFrenteUri.toString());
        intent.putExtra("foto_dorso",  photoDorsoUri.toString());

        startActivity(intent);
        // No llamamos finish() para que el usuario pueda volver con Back
    }
}