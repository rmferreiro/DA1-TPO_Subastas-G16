package tpo.g16.blackwood;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.Calendar;

public class EmpleadoCrearSubastaActivity extends AppCompatActivity {

    private EditText inputFecha;
    private EditText inputHora;
    private EditText inputCiudad;
    private EditText inputSala;
    private EditText inputEstimacion;
    private EditText inputLotes;
    private Spinner spinnerRematador;

    private View[] categoriaBtns;

    // Fecha real elegida en el DatePicker. inputFecha solo muestra el texto formateado.
    private LocalDate fechaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_crear_subasta);

        inputFecha = findViewById(R.id.input_fecha);
        inputHora = findViewById(R.id.input_hora);
        inputCiudad = findViewById(R.id.input_ciudad);
        inputSala = findViewById(R.id.input_sala);
        inputEstimacion = findViewById(R.id.input_estimacion);
        inputLotes = findViewById(R.id.input_lotes);
        spinnerRematador = findViewById(R.id.spinner_rematador);

        // ── SPINNER REMATADOR (mismos datos que en Filtros) ──────────
        String[] rematadores = {"Ruiz", "López", "Gómez", "Pérez"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                rematadores
        );
        spinnerRematador.setAdapter(adapter);

        // ── CATEGORÍA (chips, igual a FiltrosBottomSheet) ────────────
        View btnComun = findViewById(R.id.btn_comun);
        View btnEspecial = findViewById(R.id.btn_especial);
        View btnPlata = findViewById(R.id.btn_plata);
        View btnOro = findViewById(R.id.btn_oro);
        View btnPlatino = findViewById(R.id.btn_platino);

        categoriaBtns = new View[]{btnComun, btnEspecial, btnPlata, btnOro, btnPlatino};
        seleccionarCategoria(btnComun);

        for (View btn : categoriaBtns) {
            btn.setOnClickListener(v -> seleccionarCategoria(v));
        }

        // ── DATE PICKER ───────────────────────────────────────────────
        inputFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (datePicker, year, month, day) -> {
                fechaSeleccionada = LocalDate.of(year, month + 1, day);
                inputFecha.setText(FechaUtils.formatear(fechaSeleccionada));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── TIME PICKER ───────────────────────────────────────────────
        inputHora.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                String hora = String.format("%02d:%02d", hour, minute);
                inputHora.setText(hora);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        // ── BOTÓN VOLVER ──────────────────────────────────────────────
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // ── BOTÓN CREAR SUBASTA ───────────────────────────────────────
        findViewById(R.id.btn_crear_subasta).setOnClickListener(v -> intentarCrearSubasta());

        // ── BARRA DE NAVEGACIÓN ───────────────────────────────────────
        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
            finish();
        });
        findViewById(R.id.nav_mis_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }

    private void seleccionarCategoria(View seleccionado) {
        for (View btn : categoriaBtns) {
            btn.setBackgroundResource(R.drawable.input_bg);
            ((TextView) btn).setTextColor(0xFF1A1A1A);
        }
        seleccionado.setBackgroundResource(R.drawable.button_primary);
        ((TextView) seleccionado).setTextColor(0xFFF4F1EA);
    }

    private void intentarCrearSubasta() {
        String hora = inputHora.getText().toString().trim();
        String ciudad = inputCiudad.getText().toString().trim();
        String sala = inputSala.getText().toString().trim();
        String estimacion = inputEstimacion.getText().toString().trim();
        String lotes = inputLotes.getText().toString().trim();

        if (fechaSeleccionada == null || hora.isEmpty() || ciudad.isEmpty() || sala.isEmpty()
                || estimacion.isEmpty() || lotes.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Datos incompletos")
                    .setMessage("Completá todos los campos antes de crear la subasta.")
                    .setPositiveButton("Entendido", null)
                    .show();
            return;
        }

        String rematador = (String) spinnerRematador.getSelectedItem();
        String categoria = obtenerCategoriaSeleccionada();
        String estimacionFormateada = formatearMiles(estimacion);

        Subasta nueva = new Subasta(
                SubastaRepository.getInstance().generarProximoId(),
                Subasta.PROXIMA, fechaSeleccionada, hora, ciudad, sala,
                rematador, categoria, estimacionFormateada, lotes
        );

        // Guarda la subasta a través del repositorio. Cuando se conecte el backend,
        // SubastaRepository.crear() es el único lugar que hay que tocar (ver TODO ahí).
        SubastaRepository.getInstance().crear(nueva);

        new AlertDialog.Builder(this)
                .setTitle("Subasta creada")
                .setMessage(ciudad + " · " + sala + "\n" + FechaUtils.formatear(fechaSeleccionada) + " · " + hora
                        + "\nRematador: " + rematador + "  ·  Categoría: " + categoria
                        + "\nEstimación: USD " + estimacionFormateada + "  ·  " + lotes + " lotes")
                .setPositiveButton("Volver a subastas", (dialog, which) -> {
                    startActivity(new Intent(this, EmpleadoSubastasActivity.class));
                    finish();
                })
                .show();
    }

    /** Convierte "14000" en "14.000" (separador de miles con punto, como en el resto de la app). */
    private String formatearMiles(String numero) {
        StringBuilder resultado = new StringBuilder();
        int contador = 0;
        for (int i = numero.length() - 1; i >= 0; i--) {
            resultado.insert(0, numero.charAt(i));
            contador++;
            if (contador % 3 == 0 && i != 0) {
                resultado.insert(0, '.');
            }
        }
        return resultado.toString();
    }

    private String obtenerCategoriaSeleccionada() {
        for (View btn : categoriaBtns) {
            // El chip seleccionado es el único con fondo button_primary (texto claro)
            if (((TextView) btn).getCurrentTextColor() == 0xFFF4F1EA) {
                return ((TextView) btn).getText().toString();
            }
        }
        return "Común";
    }
}
