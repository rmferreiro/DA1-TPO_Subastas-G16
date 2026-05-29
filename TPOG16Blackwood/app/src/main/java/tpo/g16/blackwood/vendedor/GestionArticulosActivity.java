package tpo.g16.blackwood.vendedor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class GestionArticulosActivity extends AppCompatActivity {

    private LinearLayout layoutCards;
    private Button btnNuevoArticulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_articulos);

        btnNuevoArticulo = findViewById(R.id.btnNuevoArticulo);
        layoutCards = findViewById(R.id.layoutCards);

        btnNuevoArticulo.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevoLoteActivity.class);
            startActivity(intent);
        });

        cargarArticulos();
    }

    private void cargarArticulos() {
        List<Lote> lotes = LoteRepository.getInstance().getLotes();
        layoutCards.removeAllViews();

        for (Lote lote : lotes) {
            View card = getLayoutInflater().inflate(R.layout.card_articulo_gestion, layoutCards, false);

            TextView tvNombre = card.findViewById(R.id.tvCardNombre);
            TextView tvDescripcion = card.findViewById(R.id.tvCardDescripcion);
            TextView tvEstado = card.findViewById(R.id.tvCardEstado);
            TextView btnDetalle = card.findViewById(R.id.btnCardDetalle);

            tvNombre.setText(lote.getNombreArticulo());
            tvDescripcion.setText(lote.getCategoria() + " - $" + String.format("%.0f", lote.getPrecioEstimado()));
            tvEstado.setText(lote.getResumenEstado());

            btnDetalle.setOnClickListener(v -> {
                Intent intent = new Intent(this, SeguimientoSoporteActivity.class);
                intent.putExtra("lote_id", lote.getId());
                startActivity(intent);
            });

            layoutCards.addView(card);
        }
    }
}