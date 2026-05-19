package tpo.g16.blackwood.vendedor;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.EstadoLote;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.model.SeguimientoEstado;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class SeguimientoEstadoActivity extends AppCompatActivity {

    private LinearLayout layoutTimeline;
    private Lote lote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_estado);

        int loteId = getIntent().getIntExtra("lote_id", -1);
        lote = LoteRepository.getInstance().getLotePorId(loteId);
        if (lote == null) {
            finish();
            return;
        }

        layoutTimeline = findViewById(R.id.layoutTimeline);
        construirTimeline();
    }

    private void construirTimeline() {
        List<SeguimientoEstado> historial = lote.getHistorialEstados();
        EstadoLote estadoActual = lote.getEstadoActual();

        for (int i = 0; i < historial.size(); i++) {
            SeguimientoEstado item = historial.get(i);
            boolean isLast = (i == historial.size() - 1);
            boolean isCompleted = !isLast || item.getEstado() == estadoActual;

            View timelineItem = crearItemTimeline(item, isLast, isCompleted);
            layoutTimeline.addView(timelineItem);

            // Línea conectora entre items
            if (!isLast) {
                View connector = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(3, 60);
                params.setMargins(58, 0, 0, 0);
                connector.setLayoutParams(params);
                connector.setBackgroundColor(ContextCompat.getColor(this, R.color.gold));
                layoutTimeline.addView(connector);
            }
        }
    }

    private View crearItemTimeline(SeguimientoEstado item, boolean isLast, boolean isCompleted) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 8, 0, 8);

        // Círculo indicador
        TextView circle = new TextView(this);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(40, 40);
        circleParams.setMargins(20, 4, 20, 4);
        circle.setLayoutParams(circleParams);

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);

        if (isCompleted) {
            circleDrawable.setColor(ContextCompat.getColor(this, R.color.estado_aprobado));
        } else {
            circleDrawable.setColor(Color.LTGRAY);
        }
        circleDrawable.setStroke(2, ContextCompat.getColor(this, R.color.gold));
        circle.setBackground(circleDrawable);

        // Contenido
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(8, 0, 16, 0);

        // Título del estado
        TextView title = new TextView(this);
        title.setText(item.getEstado().getNombreVisual());
        title.setTextColor(ContextCompat.getColor(this, R.color.green_dark));
        title.setTextSize(15);

        // Fecha
        TextView date = new TextView(this);
        date.setText(item.getFecha());
        date.setTextColor(Color.GRAY);
        date.setTextSize(12);

        // Descripción
        TextView desc = new TextView(this);
        desc.setText(item.getDescripcion());
        desc.setTextColor(Color.GRAY);
        desc.setTextSize(13);
        desc.setPadding(0, 4, 0, 0);

        contentLayout.addView(title);
        contentLayout.addView(date);
        contentLayout.addView(desc);

        container.addView(circle);
        container.addView(contentLayout);

        return container;
    }
}