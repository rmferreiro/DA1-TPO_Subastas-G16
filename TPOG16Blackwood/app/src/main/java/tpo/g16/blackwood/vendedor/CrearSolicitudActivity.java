package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.EstadoLote;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class CrearSolicitudActivity extends AppCompatActivity {

    private EditText etNombre, etCategoria, etDescripcion, etEstadoProducto, etPrecio;
    private CheckBox cbDeclaracion;
    private Button btnFotos, btnEnviar;
    private TextView tvFotosCount;

    private int fotosSeleccionadas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_solicitud);

        etNombre = findViewById(R.id.etNombreArticulo);
        etCategoria = findViewById(R.id.etCategoria);
        etDescripcion = findViewById(R.id.etDescripcion);
        etEstadoProducto = findViewById(R.id.etEstadoProducto);
        etPrecio = findViewById(R.id.etPrecioEstimado);
        cbDeclaracion = findViewById(R.id.cbDeclaracionPropiedad);
        btnFotos = findViewById(R.id.btnAgregarFotos);
        btnEnviar = findViewById(R.id.btnEnviarSolicitud);
        tvFotosCount = findViewById(R.id.tvFotosCount);

        btnFotos.setOnClickListener(v -> {
            fotosSeleccionadas = 3; // mock
            tvFotosCount.setText(fotosSeleccionadas + " fotos seleccionadas");
        });

        btnEnviar.setOnClickListener(v -> {
            if (validarFormulario()) {
                String nombre = etNombre.getText().toString().trim();
                String categoria = etCategoria.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();
                String estadoProducto = etEstadoProducto.getText().toString().trim();
                double precio = Double.parseDouble(etPrecio.getText().toString().trim());

                Lote nuevoLote = new Lote(
                        0, nombre, categoria, descripcion, estadoProducto,
                        precio, java.time.LocalDate.now().toString(),
                        new ArrayList<>(), EstadoLote.SOLICITUD_EN_PROCESO
                );

                LoteRepository.getInstance().agregarLote(nuevoLote);

                Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private boolean validarFormulario() {
        String nombre = etNombre.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Campo obligatorio");
            etNombre.requestFocus();
            return false;
        }
        if (categoria.isEmpty()) {
            etCategoria.setError("Campo obligatorio");
            etCategoria.requestFocus();
            return false;
        }
        if (descripcion.isEmpty()) {
            etDescripcion.setError("Campo obligatorio");
            etDescripcion.requestFocus();
            return false;
        }
        if (precioStr.isEmpty()) {
            etPrecio.setError("Campo obligatorio");
            etPrecio.requestFocus();
            return false;
        }
        if (Double.parseDouble(precioStr) <= 0) {
            etPrecio.setError("El precio debe ser mayor a cero");
            etPrecio.requestFocus();
            return false;
        }
        if (fotosSeleccionadas < 1) {
            Toast.makeText(this, "Debe agregar al menos 1 foto", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!cbDeclaracion.isChecked()) {
            Toast.makeText(this, "Debe declarar que el bien le pertenece", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}