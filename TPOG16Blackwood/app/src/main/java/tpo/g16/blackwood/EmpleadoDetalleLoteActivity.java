package tpo.g16.blackwood;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoDetalleLoteActivity extends AppCompatActivity {

    private Lote lote;

    private TextView txtEstado;
    private TextView txtMotivo;
    private TextView txtPropuesta;
    private TextView txtUbicacionFinal;
    private TextView txtPolizaFinal;

    private LinearLayout groupDecision;
    private LinearLayout groupPropuesta;
    private LinearLayout groupPostventa;
    private LinearLayout groupFinal;

    private EditText inputUbicacion;
    private EditText inputPoliza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_detalle_lote);

        // El lote viaja por id: el objeto real vive en LoteRepository (listo para
        // cuando ese repositorio pase a consultar el backend en vez de memoria).
        int loteId = getIntent().getIntExtra("loteId", -1);
        lote = LoteRepository.getInstance().obtenerPorId(loteId);

        if (lote == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txt_nombre_lote)).setText(lote.getNombre());
        ((TextView) findViewById(R.id.txt_duenio_lote)).setText("Dueño: " + lote.getDuenio());
        ((TextView) findViewById(R.id.txt_valor_lote)).setText("Valor estimado: USD " + lote.getValorEstimado());

        txtEstado = findViewById(R.id.txt_estado);
        txtMotivo = findViewById(R.id.txt_motivo);
        txtPropuesta = findViewById(R.id.txt_propuesta);
        txtUbicacionFinal = findViewById(R.id.txt_ubicacion_final);
        txtPolizaFinal = findViewById(R.id.txt_poliza_final);

        groupDecision = findViewById(R.id.group_decision);
        groupPropuesta = findViewById(R.id.group_propuesta);
        groupPostventa = findViewById(R.id.group_postventa);
        groupFinal = findViewById(R.id.group_final);

        inputUbicacion = findViewById(R.id.input_ubicacion);
        inputPoliza = findViewById(R.id.input_poliza);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        // Botón aceptar (pasa de PENDIENTE_INSPECCION a PROPUESTA_ENVIADA)
        findViewById(R.id.btn_aceptar).setOnClickListener(v -> mostrarDialogoPropuesta());

        // Botón rechazar (PENDIENTE_INSPECCION -> RECHAZADO)
        findViewById(R.id.btn_rechazar).setOnClickListener(v -> mostrarDialogoRechazo());

        // Simulación de respuesta del usuario (mientras no está conectado el backend)
        findViewById(R.id.btn_simular_aceptado).setOnClickListener(v -> {
            lote.setEstado(EstadoLote.ACEPTADO_USUARIO);
            guardarYActualizarVista();
        });

        findViewById(R.id.btn_simular_rechazado).setOnClickListener(v -> {
            lote.setEstado(EstadoLote.RECHAZADO_USUARIO);
            lote.setMotivoRechazo("El usuario no aceptó el valor base / comisión propuestos.");
            guardarYActualizarVista();
        });

        // Guardar ubicación + póliza e incluir en subasta
        findViewById(R.id.btn_incluir_subasta).setOnClickListener(v -> {
            String ubicacion = inputUbicacion.getText().toString().trim();
            String poliza = inputPoliza.getText().toString().trim();
            if (ubicacion.isEmpty() || poliza.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Datos incompletos")
                        .setMessage("Completá la ubicación y el número de póliza antes de incluir el lote en una subasta.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }
            lote.setUbicacion(ubicacion);
            lote.setPoliza(poliza);
            lote.setEstado(EstadoLote.INCLUIDO_SUBASTA);
            guardarYActualizarVista();
        });

        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
        findViewById(R.id.nav_mis_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });

        actualizarVista();
    }

    private void mostrarDialogoPropuesta() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);

        EditText inputValorBase = new EditText(this);
        inputValorBase.setHint("Valor base (USD)");
        inputValorBase.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        container.addView(inputValorBase);

        EditText inputComision = new EditText(this);
        inputComision.setHint("Comisión (%)");
        inputComision.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        container.addView(inputComision);

        new AlertDialog.Builder(this)
                .setTitle("Aceptar lote y enviar propuesta")
                .setMessage("Indicá el valor base y la comisión a proponer al dueño del bien:")
                .setView(container)
                .setPositiveButton("Enviar propuesta", (dialog, which) -> {
                    String vb = inputValorBase.getText().toString().trim();
                    String com = inputComision.getText().toString().trim();
                    if (vb.isEmpty() || com.isEmpty()) {
                        return;
                    }
                    lote.setValorBasePropuesto(vb);
                    lote.setComisionPropuesta(com);
                    lote.setEstado(EstadoLote.PROPUESTA_ENVIADA);
                    guardarYActualizarVista();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoRechazo() {
        EditText inputMotivo = new EditText(this);
        inputMotivo.setHint("Ingresá el motivo del rechazo");

        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 50;
        params.rightMargin = 50;
        params.topMargin = 20;
        inputMotivo.setLayoutParams(params);
        container.addView(inputMotivo);

        new AlertDialog.Builder(this)
                .setTitle("Rechazar lote")
                .setMessage("Indicá el motivo del rechazo:")
                .setView(container)
                .setPositiveButton("Confirmar rechazo", (dialog, which) -> {
                    String motivo = inputMotivo.getText().toString();
                    if (!motivo.isEmpty()) {
                        lote.setMotivoRechazo(motivo);
                        lote.setEstado(EstadoLote.RECHAZADO);
                        guardarYActualizarVista();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Persiste los cambios a través del repositorio (hoy es un no-op porque Lote ya
     * se modifica en memoria, pero deja el punto de conexión listo para el backend)
     * y refresca la pantalla.
     */
    private void guardarYActualizarVista() {
        LoteRepository.getInstance().actualizar(lote);
        actualizarVista();
    }

    /**
     * Muestra/oculta los bloques de la pantalla según el estado actual del lote
     * y actualiza los textos correspondientes (chip de estado, motivo, propuesta, etc).
     */
    private void actualizarVista() {
        EstadoLote estadoActual = lote.getEstado();
        txtEstado.setText(estadoActual.getEtiqueta());
        txtEstado.setBackgroundColor(estadoActual.getColor());

        // Por defecto todos los bloques ocultos, se muestra solo el que corresponde
        groupDecision.setVisibility(View.GONE);
        groupPropuesta.setVisibility(View.GONE);
        groupPostventa.setVisibility(View.GONE);
        groupFinal.setVisibility(View.GONE);
        txtMotivo.setVisibility(View.GONE);

        switch (estadoActual) {
            case PENDIENTE_INSPECCION:
                groupDecision.setVisibility(View.VISIBLE);
                break;

            case RECHAZADO:
                txtMotivo.setText("Motivo del rechazo: " + lote.getMotivoRechazo());
                txtMotivo.setVisibility(View.VISIBLE);
                break;

            case PROPUESTA_ENVIADA:
                groupPropuesta.setVisibility(View.VISIBLE);
                txtPropuesta.setText("Valor base: USD " + lote.getValorBasePropuesto()
                        + "  ·  Comisión: " + lote.getComisionPropuesta() + "%");
                break;

            case RECHAZADO_USUARIO:
                txtMotivo.setText("Motivo: " + lote.getMotivoRechazo() + " El bien se devuelve a su dueño.");
                txtMotivo.setVisibility(View.VISIBLE);
                break;

            case ACEPTADO_USUARIO:
                groupPostventa.setVisibility(View.VISIBLE);
                break;

            case INCLUIDO_SUBASTA:
                groupFinal.setVisibility(View.VISIBLE);
                txtUbicacionFinal.setText("Ubicación: " + lote.getUbicacion());
                txtPolizaFinal.setText("Póliza de seguro: " + lote.getPoliza());
                break;
        }
    }
}
