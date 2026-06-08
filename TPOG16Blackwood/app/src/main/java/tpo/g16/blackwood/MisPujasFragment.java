package tpo.g16.blackwood;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class MisPujasFragment extends Fragment {

    private RecyclerView recyclerView;
    private MisPujasAdapter adapter;
    private TextView tvParticipaciones;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mis_pujas, container, false);

        // Ocultar el bottom_nav incluido en el layout, ya que HomeActivity tiene el suyo
        View bottomNav = view.findViewById(R.id.bottom_nav_include);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        } else {
            // Intento por si no tiene el ID explícito
            View tab = view.findViewById(R.id.tab_subastas);
            if (tab != null && tab.getParent() != null && tab.getParent() instanceof ViewGroup) {
                View parent = (View) tab.getParent();
                if (parent.getParent() != null && parent.getParent() instanceof ViewGroup) {
                    ((View) parent.getParent()).setVisibility(View.GONE);
                }
            }
        }

        tvParticipaciones = view.findViewById(R.id.tv_participaciones);
        recyclerView = view.findViewById(R.id.rv_mis_pujas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new MisPujasAdapter(getContext());
        recyclerView.setAdapter(adapter);

        cargarMisPujas();

        return view;
    }

    private void cargarMisPujas() {
        RetrofitClient.getApiService().getMisPujas().enqueue(new Callback<List<MiPuja>>() {
            @Override
            public void onResponse(Call<List<MiPuja>> call, Response<List<MiPuja>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MiPuja> pujas = response.body();
                    adapter.setPujas(pujas);
                    if (tvParticipaciones != null) {
                        tvParticipaciones.setText("Participaste en " + pujas.size() + " subastas");
                    }
                } else {
                    if (tvParticipaciones != null) tvParticipaciones.setText("Error al cargar");
                }
            }

            @Override
            public void onFailure(Call<List<MiPuja>> call, Throwable t) {
                Log.e("MisPujas", "Error: ", t);
                if (tvParticipaciones != null) tvParticipaciones.setText("Error de red");
            }
        });
    }
}
