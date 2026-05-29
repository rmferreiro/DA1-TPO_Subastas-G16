package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.Lote;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class PostAprobacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_aprobacion);

        int loteId = getIntent().getIntExtra("lote_id", -1);
        Lote lote = LoteRepository.getInstance().getLotePorId(loteId);

        TextView tvTitulo = findViewById(R.id.tvPostTitulo);
        if (lote != null) {
            tvTitulo.setText(lote.getNombreArticulo());
        }
    }
}