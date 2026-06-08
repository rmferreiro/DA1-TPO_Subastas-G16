package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.SubastaResponse;

public class ListaSubastasFragment extends Fragment {

    private RecyclerView recyclerSubastas;
    private SubastaAdapter adapter;
    private List<SubastaResponse> todasLasSubastas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_lista_subastas, container, false);

        recyclerSubastas = view.findViewById(R.id.recycler_subastas);
        recyclerSubastas.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new SubastaAdapter(new ArrayList<>());
        recyclerSubastas.setAdapter(adapter);

        configurarFiltros(view);
        cargarSubastas();

        return view;
    }

    private void configurarFiltros(View view) {
        android.widget.TextView chipTodas = view.findViewById(R.id.chip_todas);
        android.widget.TextView chipOro = view.findViewById(R.id.chip_oro);
        android.widget.TextView chipPlatino = view.findViewById(R.id.chip_platino);
        android.widget.TextView chipDiamante = view.findViewById(R.id.chip_diamante);
        android.widget.TextView chipComun = view.findViewById(R.id.chip_comun);

        View.OnClickListener filterListener = v -> {
            // Reset styles
            chipTodas.setBackgroundResource(R.drawable.chip_outline_gold_border);
            chipTodas.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));
            
            chipOro.setBackgroundResource(R.drawable.chip_outline_gold_border);
            chipOro.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));

            chipPlatino.setBackgroundResource(R.drawable.chip_outline_gold_border);
            chipPlatino.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));

            chipDiamante.setBackgroundResource(R.drawable.chip_outline_gold_border);
            chipDiamante.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));

            chipComun.setBackgroundResource(R.drawable.chip_outline_gold_border);
            chipComun.setTextColor(android.graphics.Color.parseColor("#6B6B6B"));

            // Set active style
            android.widget.TextView selected = (android.widget.TextView) v;
            selected.setBackgroundResource(R.drawable.chip_dark);
            selected.setTextColor(android.graphics.Color.parseColor("#F4F1EA"));

            // Filter data
            String filter = selected.getText().toString().toUpperCase();
            if ("TODAS".equals(filter)) {
                adapter.updateData(todasLasSubastas);
            } else {
                // Support exact match, handling accent mapping manually if needed
                String filterValue = filter.equals("COMÚN") ? "COMUN" : filter;
                List<SubastaResponse> filtered = new ArrayList<>();
                for (SubastaResponse subasta : todasLasSubastas) {
                    if (subasta.getCategoria() != null && filterValue.equals(subasta.getCategoria().toUpperCase())) {
                        filtered.add(subasta);
                    }
                }
                adapter.updateData(filtered);
            }
        };

        chipTodas.setOnClickListener(filterListener);
        chipOro.setOnClickListener(filterListener);
        chipPlatino.setOnClickListener(filterListener);
        chipDiamante.setOnClickListener(filterListener);
        chipComun.setOnClickListener(filterListener);
    }

    private void cargarSubastas() {
        RetrofitClient.getApiService().getSubastasDisponibles().enqueue(new Callback<List<SubastaResponse>>() {
            @Override
            public void onResponse(Call<List<SubastaResponse>> call, Response<List<SubastaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todasLasSubastas = response.body();
                    adapter.updateData(todasLasSubastas);
                } else {
                    Toast.makeText(getContext(), "Error al cargar subastas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubastaResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
