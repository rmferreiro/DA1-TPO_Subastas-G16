package tpo.g16.blackwood.register;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

public class NuevoLoteActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_PICK = 102;

    private ImageView imgFoto1, imgFoto2, imgFoto3, imgFoto4, imgFoto5, imgFoto6;
    private LinearLayout btnAddFoto;
    private EditText etNombre, etSubtitulo, etPrecio, etDescripcion;
    private TextView btnMonedaArs, btnMonedaUsd;
    private SwitchMaterial swObraArte;
    private LinearLayout containerObraArte;
    private EditText etArtista, etFecha, etHistoria;
    private CheckBox cbDeclaracion, cbOrigen;
    private MaterialButton btnEnviar;

    private final List<String> fotosBase64 = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    private String monedaSeleccionada = "ARS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_lote);

        // Configurar subtítulo del Header
        TextView headerSubtitle = findViewById(R.id.header_subtitle);
        if (headerSubtitle != null) {
            headerSubtitle.setText("Nuevo lote");
        }

        // Vincular componentes de fotos
        imgFoto1 = findViewById(R.id.img_foto_1);
        imgFoto2 = findViewById(R.id.img_foto_2);
        imgFoto3 = findViewById(R.id.img_foto_3);
        imgFoto4 = findViewById(R.id.img_foto_4);
        imgFoto5 = findViewById(R.id.img_foto_5);
        imgFoto6 = findViewById(R.id.img_foto_6);
        btnAddFoto = findViewById(R.id.btn_add_foto);

        etNombre = findViewById(R.id.et_nombre_lote);
        etSubtitulo = findViewById(R.id.et_subtitulo_lote);
        etPrecio = findViewById(R.id.et_precio_lote);
        etDescripcion = findViewById(R.id.et_descripcion_lote);
        
        btnMonedaArs = findViewById(R.id.btn_moneda_ars);
        btnMonedaUsd = findViewById(R.id.btn_moneda_usd);

        swObraArte = findViewById(R.id.sw_obra_arte);
        containerObraArte = findViewById(R.id.container_obra_arte);
        etArtista = findViewById(R.id.et_artista_obra);
        etFecha = findViewById(R.id.et_fecha_obra);
        etHistoria = findViewById(R.id.et_historia_obra);

        cbDeclaracion = findViewById(R.id.cb_declaracion_jurada);
        cbOrigen = findViewById(R.id.cb_origen_licito);
        btnEnviar = findViewById(R.id.btn_enviar_evaluacion);

        // Configurar selector de moneda personalizado (Segmented Control)
        btnMonedaArs.setOnClickListener(v -> seleccionarMoneda("ARS"));
        btnMonedaUsd.setOnClickListener(v -> seleccionarMoneda("USD"));
        seleccionarMoneda("ARS"); // Default

        // Manejar agregado de fotos
        btnAddFoto.setOnClickListener(v -> mostrarOpcionesFoto());

        // Toggle obra de arte
        swObraArte.setOnCheckedChangeListener((buttonView, isChecked) -> {
            containerObraArte.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            validarFormulario();
        });

        // Configurar selector de fecha
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        // Listeners de validación
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarFormulario();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etNombre.addTextChangedListener(validationWatcher);
        etSubtitulo.addTextChangedListener(validationWatcher);
        etPrecio.addTextChangedListener(validationWatcher);
        etDescripcion.addTextChangedListener(validationWatcher);
        etArtista.addTextChangedListener(validationWatcher);
        etFecha.addTextChangedListener(validationWatcher);
        etHistoria.addTextChangedListener(validationWatcher);

        cbDeclaracion.setOnCheckedChangeListener((buttonView, isChecked) -> validarFormulario());
        cbOrigen.setOnCheckedChangeListener((buttonView, isChecked) -> validarFormulario());

        btnEnviar.setOnClickListener(v -> enviarEvaluacion());

        // Inicializar validación inicial del botón
        validarFormulario();
    }

    private void seleccionarMoneda(String moneda) {
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
    }

    private void mostrarOpcionesFoto() {
        if (fotosBase64.size() >= 6) {
            Toast.makeText(this, "Ya cargaste el máximo de 6 fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] opciones = {"Tomar foto (Cámara)", "Seleccionar de galería"};
        new AlertDialog.Builder(this)
                .setTitle("Agregar foto del lote")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        try {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            } else {
                                Toast.makeText(this, "No se encontró cámara disponible", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Error al abrir cámara", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error al abrir galería", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                    bitmap = BitmapFactory.decodeStream(imageStream);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }

            if (bitmap != null) {
                Bitmap scaled = scaleBitmap(bitmap, 800);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bytes = baos.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                if (fotosBase64.size() < 6) {
                    fotosBase64.add(base64);
                    int index = fotosBase64.size() - 1;
                    ImageView targetView = null;
                    if (index == 0) targetView = imgFoto1;
                    else if (index == 1) targetView = imgFoto2;
                    else if (index == 2) targetView = imgFoto3;
                    else if (index == 3) targetView = imgFoto4;
                    else if (index == 4) targetView = imgFoto5;
                    else if (index == 5) targetView = imgFoto6;

                    if (targetView != null) {
                        targetView.setImageBitmap(scaled);
                        targetView.setBackground(null);
                        targetView.setImageTintList(null);
                        targetView.setPadding(0, 0, 0, 0);
                    }
                    validarFormulario();
                    Toast.makeText(this, "Foto " + (index + 1) + " agregada con éxito", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }
        float ratio = (float) width / (float) height;
        int newWidth, newHeight;
        if (ratio > 1) {
            newWidth = maxDimension;
            newHeight = (int) (maxDimension / ratio);
        } else {
            newHeight = maxDimension;
            newWidth = (int) (maxDimension * ratio);
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
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

    private void validarFormulario() {
        boolean baseValida = !etNombre.getText().toString().trim().isEmpty() &&
                !etSubtitulo.getText().toString().trim().isEmpty() &&
                !etPrecio.getText().toString().trim().isEmpty() &&
                !etDescripcion.getText().toString().trim().isEmpty() &&
                fotosBase64.size() >= 1 &&
                cbDeclaracion.isChecked() &&
                cbOrigen.isChecked();

        boolean obraArteValida = true;
        if (swObraArte.isChecked()) {
            obraArteValida = !etArtista.getText().toString().trim().isEmpty() &&
                    !etFecha.getText().toString().trim().isEmpty() &&
                    !etHistoria.getText().toString().trim().isEmpty();
        }

        boolean habilitado = baseValida && obraArteValida;
        btnEnviar.setEnabled(habilitado);

        if (habilitado) {
            btnEnviar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1C2A21")));
            btnEnviar.setTextColor(Color.WHITE);
        } else {
            btnEnviar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // Gris claro
            btnEnviar.setTextColor(Color.parseColor("#757575")); // Gris oscuro
        }
    }

    private void enviarEvaluacion() {
        btnEnviar.setEnabled(false);

        Map<String, Object> request = new HashMap<>();
        request.put("descripcion", etNombre.getText().toString().trim());
        request.put("subtitulo", etSubtitulo.getText().toString().trim());
        request.put("descripcionLarga", etDescripcion.getText().toString().trim());
        request.put("tipo", swObraArte.isChecked() ? "OBRA_ARTE" : "ESTANDAR");
        request.put("precioEstimado", Double.parseDouble(etPrecio.getText().toString().trim()));
        request.put("moneda", monedaSeleccionada);
        request.put("declaracionJurada", true);
        request.put("fotos", fotosBase64);

        if (swObraArte.isChecked()) {
            request.put("artista", etArtista.getText().toString().trim());
            request.put("fechaCreacion", etFecha.getText().toString().trim());
            request.put("historia", etHistoria.getText().toString().trim());
        }

        RetrofitClient.getApiService()
                .solicitarProducto(request)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Double idDouble = (Double) response.body().get("productoId");
                            int id = idDouble != null ? idDouble.intValue() : 0;
                            
                            Toast.makeText(NuevoLoteActivity.this, "Lote enviado para evaluación", Toast.LENGTH_SHORT).show();
                            
                            Intent intent = new Intent(NuevoLoteActivity.this, SeguimientoLoteActivity.class);
                            intent.putExtra("productoId", id);
                            startActivity(intent);
                            finish();
                        } else {
                            btnEnviar.setEnabled(true);
                            Toast.makeText(NuevoLoteActivity.this, "Error al enviar evaluación", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnEnviar.setEnabled(true);
                        Toast.makeText(NuevoLoteActivity.this, "Falla de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
