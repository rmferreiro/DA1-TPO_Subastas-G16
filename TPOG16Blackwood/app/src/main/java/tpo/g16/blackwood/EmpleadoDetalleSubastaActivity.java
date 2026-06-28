package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoDetalleSubastaActivity extends AppCompatActivity {

    // Estados posibles: NO_INICIADA, EN_PROCESO, TERMINADA
    public static final String SUBASTA_ID = "subastaId";
    public static final int NO_INICIADA = 0;
    public static final int EN_PROCESO = 1;
    public static final int TERMINADA = 2;

    private Subasta subasta;

    private Button btnPrincipal;
    private Button btnFinalizar;
    private LinearLayout containerLotesDestacados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_detalle_subasta);

        int subastaId = getIntent().getIntExtra(SUBASTA_ID, -1);
        subasta = SubastaRepository.getInstance().obtenerPorId(subastaId);

        if (subasta == null) {
            finish();
            return;
        }

        btnPrincipal = findViewById(R.id.btn_accion_principal);
        btnFinalizar = findViewById(R.id.btn_finalizar);
        containerLotesDestacados = findViewById(R.id.container_lotes_destacados);

        // Datos del header, sacados de la Subasta real (no más hardcodeados)
        ((TextView) findViewById(R.id.txt_fecha_hora)).setText(subasta.getFechaFormateada() + " · " + subasta.getHora());
        ((TextView) findViewById(R.id.txt_ubicacion)).setText(subasta.getCiudad() + " · " + subasta.getSala());
        ((TextView) findViewById(R.id.txt_rematador)).setText("Rematador: " + subasta.getRematador());
        ((TextView) findViewById(R.id.txt_lotes_disponibles)).setText(subasta.getCantidadLotes() + " lotes disponibles");
        ((TextView) findViewById(R.id.txt_estimacion_inicial)).setText("Estimación inicial: " + subasta.getEstimacion() + " USD");
        ((TextView) findViewById(R.id.txt_incremento_minimo)).setText("Incremento mínimo: " + subasta.getIncrementoMinimo() + " USD");
        ((TextView) findViewById(R.id.txt_categoria)).setText(subasta.getCategoria().toUpperCase());
        actualizarBadgeEstado();

        renderizarLotesDestacados();
        actualizarBotones();

        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        btnPrincipal.setOnClickListener(v -> {
            if (subasta.getEstado() == NO_INICIADA) {
                subasta.setEstado(EN_PROCESO);
                actualizarBotones();
                actualizarBadgeEstado();
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            subasta.setEstado(TERMINADA);
            actualizarBotones();
            actualizarBadgeEstado();
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
    }

    private void renderizarLotesDestacados() {
        containerLotesDestacados.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (LoteDestacado lote : subasta.getLotesDestacados()) {
            View card = inflater.inflate(R.layout.item_lote_destacado, containerLotesDestacados, false);
            ((TextView) card.findViewById(R.id.txt_numero)).setText(lote.getNumero());
            ((TextView) card.findViewById(R.id.txt_nombre)).setText(lote.getNombre());
            ((TextView) card.findViewById(R.id.txt_estimacion)).setText(lote.getEstimacion());
            containerLotesDestacados.addView(card);
        }
    }

    private void actualizarBadgeEstado() {
        TextView txtBadge = findViewById(R.id.txt_badge_estado);
        View dot = findViewById(R.id.dot_estado);

        switch (subasta.getEstado()) {
            case NO_INICIADA:
                txtBadge.setText("Próxima · No iniciada");
                dot.setBackgroundColor(0xFFC6A75E); // dorado
                break;
            case EN_PROCESO:
                txtBadge.setText("En sala · Activa");
                dot.setBackgroundColor(0xFF4CAF50); // verde
                break;
            case TERMINADA:
                txtBadge.setText("Finalizada");
                dot.setBackgroundColor(0xFF888888); // gris
                break;
        }
    }

    private void actualizarBotones() {
        int estado = subasta.getEstado();
        switch (estado) {
            case NO_INICIADA:
                // Verde: Empezar Subasta + gris: Finalizar Subasta
                btnPrincipal.setVisibility(View.VISIBLE);
                btnPrincipal.setText("Empezar Subasta");
                btnPrincipal.setBackgroundResource(R.drawable.button_primary);
                btnPrincipal.setBackgroundTintList(null);
                btnFinalizar.setVisibility(View.VISIBLE);
                btnFinalizar.setText("Finalizar Subasta");
                btnFinalizar.setBackgroundResource(R.drawable.button_black_gold);
                btnFinalizar.setBackgroundTintList(null);
                break;

            case EN_PROCESO:
                // Dorado: Unirse a la puja + rojo: Finalizar Subasta
                btnPrincipal.setVisibility(View.VISIBLE);
                btnPrincipal.setText("Unirse a la puja");
                btnPrincipal.setBackgroundResource(R.drawable.button_gold);
                btnPrincipal.setBackgroundTintList(null);
                btnFinalizar.setVisibility(View.VISIBLE);
                btnFinalizar.setText("Finalizar Subasta");
                btnFinalizar.setBackgroundResource(R.drawable.button_danger);
                btnFinalizar.setBackgroundTintList(null);
                break;

            case TERMINADA:
                // Sin botones
                btnPrincipal.setVisibility(View.GONE);
                btnFinalizar.setVisibility(View.GONE);
                break;
        }
    }
}
