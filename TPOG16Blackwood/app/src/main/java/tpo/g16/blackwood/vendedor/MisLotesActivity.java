package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class MisLotesActivity extends AppCompatActivity {

    private RecyclerView rvLotes;
    private LoteAdapter adapter;
    private LoteRepository repository;
    private Button filtroTodos, filtroEnProceso, filtroAprobados, filtroRechazados, filtroVendidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_lotes);

        repository = LoteRepository.getInstance();

        rvLotes = findViewById(R.id.rvLotes);
        rvLotes.setLayoutManager(new LinearLayoutManager(this));

        filtroTodos = findViewById(R.id.filtroTodos);
        filtroEnProceso = findViewById(R.id.filtroEnProceso);
        filtroAprobados = findViewById(R.id.filtroAprobados);
        filtroRechazados = findViewById(R.id.filtroRechazados);
        filtroVendidos = findViewById(R.id.filtroVendidos);

        adapter = new LoteAdapter(repository.getLotes());
        rvLotes.setAdapter(adapter);

        filtroTodos.setOnClickListener(v -> {
            adapter.actualizarLista(repository.getLotes());
            resetFilterButtons(filtroTodos);
        });

        filtroEnProceso.setOnClickListener(v -> {
            adapter.actualizarLista(repository.getLotesPorFiltro("en proceso"));
            resetFilterButtons(filtroEnProceso);
        });

        filtroAprobados.setOnClickListener(v -> {
            adapter.actualizarLista(repository.getLotesPorFiltro("aprobados"));
            resetFilterButtons(filtroAprobados);
        });

        filtroRechazados.setOnClickListener(v -> {
            adapter.actualizarLista(repository.getLotesPorFiltro("rechazados"));
            resetFilterButtons(filtroRechazados);
        });

        filtroVendidos.setOnClickListener(v -> {
            adapter.actualizarLista(repository.getLotesPorFiltro("vendidos"));
            resetFilterButtons(filtroVendidos);
        });
    }

    private void resetFilterButtons(Button active) {
        Button[] buttons = {filtroTodos, filtroEnProceso, filtroAprobados, filtroRechazados, filtroVendidos};
        for (Button b : buttons) {
            if (b == active) {
                b.setBackgroundResource(R.drawable.button_primary);
                b.setTextColor(getColor(R.color.cream));
            } else {
                b.setBackgroundResource(R.drawable.button_black_gold);
                b.setTextColor(getColor(R.color.black));
            }
        }
    }
}