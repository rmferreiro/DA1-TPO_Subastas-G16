package tpo.g16.blackwood;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.MiPuja;

public class HistorialFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistorialAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        recyclerView = view.findViewById(R.id.rv_historial);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new HistorialAdapter(getContext());
        recyclerView.setAdapter(adapter);

        cargarHistorial();

        return view;
    }

    private void cargarHistorial() {
        RetrofitClient.getApiService().getMisPujas().enqueue(new Callback<List<MiPuja>>() {
            @Override
            public void onResponse(Call<List<MiPuja>> call, Response<List<MiPuja>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setPujas(response.body());
                } else {
                    Toast.makeText(getContext(), "Error al cargar historial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MiPuja>> call, Throwable t) {
                Log.e("HistorialFragment", "Error: ", t);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
