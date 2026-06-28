package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EmpleadoRevisionLotesActivity extends AppCompatActivity {

    private LinearLayout containerLotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_revision_lotes);

        containerLotes = findViewById(R.id.container_lotes);

        // Botón volver
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga cada vez que volvés a esta pantalla (por ej. después de cambiar
        // el estado de un lote en su detalle).
        renderizarLotes();
    }

    private void renderizarLotes() {
        containerLotes.removeAllViews();
        List<Lote> lotes = LoteRepository.getInstance().obtenerTodos();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Lote lote : lotes) {
            View card = inflater.inflate(R.layout.item_card_lote, containerLotes, false);

            TextView txtEstadoChip = card.findViewById(R.id.txt_estado_chip);
            TextView txtNombre = card.findViewById(R.id.txt_nombre);
            TextView txtDuenio = card.findViewById(R.id.txt_duenio);
            TextView txtValor = card.findViewById(R.id.txt_valor);

            EstadoLote estado = lote.getEstado();
            txtEstadoChip.setText(estado.getEtiqueta());
            txtEstadoChip.setBackgroundColor(estado.getColor());

            txtNombre.setText(lote.getNombre());
            txtDuenio.setText("Dueño: " + lote.getDuenio());
            txtValor.setText("Valor estimado: USD " + lote.getValorEstimado());

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, EmpleadoDetalleLoteActivity.class);
                intent.putExtra("loteId", lote.getId());
                startActivity(intent);
            });

            containerLotes.addView(card);
        }
    }
}
