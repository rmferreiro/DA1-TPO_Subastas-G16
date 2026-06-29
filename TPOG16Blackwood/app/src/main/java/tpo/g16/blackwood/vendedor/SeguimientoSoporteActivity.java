package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class SeguimientoSoporteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_soporte);

        int loteId = getIntent().getIntExtra("lote_id", -1);
        Lote lote = LoteRepository.getInstance().getLotePorId(loteId);

        TextView tvTitulo = findViewById(R.id.tvSeguimientoTitulo);
        TextView tvSub = findViewById(R.id.tvSeguimientoSub);
        Button btnContactar = findViewById(R.id.btnContactarSoporte);

        if (lote != null) {
            tvTitulo.setText(lote.getNombreArticulo());
            tvSub.setText(lote.getCategoria() + " - $" + String.format("%.0f", lote.getPrecioEstimado()));
        }

        btnContactar.setOnClickListener(v ->
            Toast.makeText(this, "Contactando al soporte...", Toast.LENGTH_SHORT).show()
        );
    }
}