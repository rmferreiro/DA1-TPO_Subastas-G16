package tpo.g16.blackwood.vendedor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import tpo.g16.blackwood.R;

public class NuevoLoteActivity extends AppCompatActivity {

    private LinearLayout layoutFotos, layoutThumbnails, layoutElementos;
    private TextView tvAddPhotoIcon, tvAddPhotoText;
    private Button btnAgregarElemento, btnEnviar;
    private EditText etDescripcion;

    private final ArrayList<Uri> fotosUris = new ArrayList<>();
    private final ArrayList<String> elementosList = new ArrayList<>();

    private Uri currentPhotoUri;

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    // Lanzador para resultado de cámara
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                    fotosUris.add(currentPhotoUri);
                    actualizarThumbnails();
                    actualizarEstadoFotos();
                }
            });

    // Lanzador para resultado de galería
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        fotosUris.add(uri);
                        actualizarThumbnails();
                        actualizarEstadoFotos();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_lote);

        layoutFotos = findViewById(R.id.layoutFotos);
        layoutThumbnails = findViewById(R.id.layoutThumbnails);
        layoutElementos = findViewById(R.id.layoutElementos);
        tvAddPhotoIcon = findViewById(R.id.tvAddPhotoIcon);
        tvAddPhotoText = findViewById(R.id.tvAddPhotoText);
        btnAgregarElemento = findViewById(R.id.btnAgregarElemento);
        btnEnviar = findViewById(R.id.btnEnviarEvaluacion);
        etDescripcion = findViewById(R.id.etDescripcionLote);

        // Click en área de fotos -> mostrar opciones
        layoutFotos.setOnClickListener(v -> mostrarDialogoFotos());

        // Click en "Agregar elemento"
        btnAgregarElemento.setOnClickListener(v -> mostrarDialogoAgregarElemento());

        // Click en "Enviar a evaluación"
        btnEnviar.setOnClickListener(v -> {
            String desc = etDescripcion.getText().toString().trim();
            if (desc.isEmpty()) {
                Toast.makeText(this, "Completá la descripción del lote", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fotosUris.size() < 3) {
                Toast.makeText(this, "Se requieren al menos 3 fotos del lote", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Lote enviado a evaluación", Toast.LENGTH_LONG).show();
            finish();
        });
    }

    // ========== FOTOS ==========

    private void mostrarDialogoFotos() {
        String[] opciones = {"Tomar foto", "Elegir de galería", "Cancelar"};
        new AlertDialog.Builder(this)
                .setTitle("Agregar foto")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        verificarPermisoCamara();
                    } else if (which == 1) {
                        abrirGaleria();
                    }
                })
                .show();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                abrirCamara();
            }
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = crearArchivoFoto();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de foto", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File crearArchivoFoto() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "LOTE_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File loteDir = new File(storageDir, "LotePhotos");
        if (!loteDir.exists()) {
            loteDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", loteDir);
    }

    private void actualizarThumbnails() {
        layoutThumbnails.removeAllViews();
        for (Uri uri : fotosUris) {
            ImageView thumb = new ImageView(this);
            int size = (int) (80 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(0, 0, 8, 0);
            thumb.setLayoutParams(lp);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setBackground(ContextCompat.getDrawable(this, R.drawable.outline_gold));

            // Cargar imagen con BitmapFactory para no depender de Glide
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(uri));
                thumb.setImageBitmap(bitmap);
            } catch (Exception e) {
                thumb.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.ic_camera, null));
            }

            // Click para eliminar foto
            thumb.setOnClickListener(v -> {
                fotosUris.remove(uri);
                actualizarThumbnails();
                actualizarEstadoFotos();
            });

            layoutThumbnails.addView(thumb);
        }
    }

    private void actualizarEstadoFotos() {
        if (fotosUris.isEmpty()) {
            layoutThumbnails.setVisibility(View.GONE);
            tvAddPhotoIcon.setVisibility(View.VISIBLE);
            tvAddPhotoText.setText("Agregar fotos del lote");
        } else {
            layoutThumbnails.setVisibility(View.VISIBLE);
            tvAddPhotoIcon.setVisibility(View.GONE);
            tvAddPhotoText.setText(fotosUris.size() + " foto(s) seleccionada(s) - Tocar para agregar más");
        }
    }

    // ========== ELEMENTOS ==========

    private void mostrarDialogoAgregarElemento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo elemento");

        final EditText input = new EditText(this);
        input.setHint("Nombre del elemento");
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = input.getText().toString().trim();
            if (!nombre.isEmpty()) {
                elementosList.add(nombre);
                agregarElementoView(nombre);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void agregarElementoView(String nombre) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvElemento = new TextView(this);
        tvElemento.setText("• " + nombre);
        tvElemento.setTextColor(0xFF6B6B6B);
        tvElemento.setTextSize(13);
        tvElemento.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ImageView btnEliminar = new ImageView(this);
        int size = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        btnEliminar.setLayoutParams(lp);
        btnEliminar.setImageDrawable(
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_delete, null));
        btnEliminar.setColorFilter(0xFFC0392B);
        btnEliminar.setOnClickListener(v -> {
            elementosList.remove(nombre);
            layoutElementos.removeView(itemLayout);
        });

        itemLayout.addView(tvElemento);
        itemLayout.addView(btnEliminar);
        itemLayout.setPadding(0, 8, 0, 8);

        layoutElementos.addView(itemLayout);
    }
}