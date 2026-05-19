package tpo.g16.blackwood.vendedor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class DetalleLoteActivity extends AppCompatActivity {

    private Lote lote;
    private TextView tvNombre, tvCategoria, tvEstado, tvDescripcion;
    private TextView tvEstadoProducto, tvPrecioEstimado, tvFecha, tvTasacion;
    private TextView tvPrecioBase, tvComision, tvFechaSubasta;
    private TextView tvMotivoRechazo, tvCostoDevolucion, tvObservaciones;
    private LinearLayout layoutTasacion, layoutSubasta, layoutRechazo;
    private Button btnSeguimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_lote);

        int loteId = getIntent().getIntExtra("lote_id", -1);
        lote = LoteRepository.getInstance().getLotePorId(loteId);
        if (lote == null) {
            finish();
            return;
        }

        tvNombre = findViewById(R.id.tvDetalleNombre);
        tvCategoria = findViewById(R.id.tvDetalleCategoria);
        tvEstado = findViewById(R.id.tvDetalleEstado);
        tvDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvEstadoProducto = findViewById(R.id.tvDetalleEstadoProducto);
        tvPrecioEstimado = findViewById(R.id.tvDetallePrecioEstimado);
        tvFecha = findViewById(R.id.tvDetalleFecha);
        tvTasacion = findViewById(R.id.tvDetalleTasacion);
        tvPrecioBase = findViewById(R.id.tvDetallePrecioBase);
        tvComision = findViewById(R.id.tvDetalleComision);
        tvFechaSubasta = findViewById(R.id.tvDetalleFechaSubasta);
        tvMotivoRechazo = findViewById(R.id.tvDetalleMotivoRechazo);
        tvCostoDevolucion = findViewById(R.id.tvDetalleCostoDevolucion);
        tvObservaciones = findViewById(R.id.tvDetalleObservaciones);
        layoutTasacion = findViewById(R.id.layoutTasacion);
        layoutSubasta = findViewById(R.id.layoutSubasta);
        layoutRechazo = findViewById(R.id.layoutRechazo);
        btnSeguimiento = findViewById(R.id.btnVerSeguimiento);

        cargarDatos();

        btnSeguimiento.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeguimientoEstadoActivity.class);
            intent.putExtra("lote_id", lote.getId());
            startActivity(intent);
        });
    }

    private void cargarDatos() {
        tvNombre.setText(lote.getNombreArticulo());
        tvCategoria.setText(lote.getCategoria());
        tvEstado.setText(lote.getResumenEstado());
        tvDescripcion.setText(lote.getDescripcion());
        tvEstadoProducto.setText(lote.getEstadoProducto());
        tvPrecioEstimado.setText("$" + String.format("%.2f", lote.getPrecioEstimado()));
        tvFecha.setText(lote.getFechaCreacion());

        // Color del estado
        switch (lote.getEstadoActual()) {
            case SOLICITUD_EN_PROCESO:
            case INICIO_TASACION:
            case EN_PROCESO:
                tvEstado.setTextColor(getColor(R.color.estado_proceso));
                break;
            case SOLICITUD_APROBADA:
            case EN_SUBASTA:
                tvEstado.setTextColor(getColor(R.color.estado_aprobado));
                break;
            case SOLICITUD_RECHAZADA:
                tvEstado.setTextColor(getColor(R.color.estado_rechazado));
                break;
            case LOTE_COMPRADO:
                tvEstado.setTextColor(getColor(R.color.estado_vendido));
                break;
        }

        // Tasación
        if (lote.getTasacion() != null) {
            layoutTasacion.setVisibility(View.VISIBLE);
            tvTasacion.setText("$" + String.format("%.2f", lote.getTasacion()));
        }

        // Datos de subasta
        if (lote.getPrecioBase() != null) {
            layoutSubasta.setVisibility(View.VISIBLE);
            tvPrecioBase.setText("$" + String.format("%.2f", lote.getPrecioBase()));
            tvComision.setText(lote.getComision() + "%");
            tvFechaSubasta.setText(lote.getFechaSubasta());
        }

        // Datos de rechazo
        if (lote.getMotivoRechazo() != null) {
            layoutRechazo.setVisibility(View.VISIBLE);
            tvMotivoRechazo.setText(lote.getMotivoRechazo());
            tvCostoDevolucion.setText("$" + String.format("%.2f", lote.getCostoDevolucion()));
            tvObservaciones.setText(lote.getObservaciones());
        }
    }
}