package tpo.g16.blackwood;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.MiPuja;

public class MisPujasFragment extends Fragment {

    private RecyclerView recyclerView;
    private MisPujasAdapter adapter;
    private View layoutSinParticipaciones;
    private View scrollMisPujas;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mis_pujas, container, false);

        layoutSinParticipaciones = view.findViewById(R.id.layout_sin_participaciones);
        scrollMisPujas = view.findViewById(R.id.scroll_mis_pujas);
        recyclerView = view.findViewById(R.id.rv_mis_pujas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MisPujasAdapter(getContext());
        recyclerView.setAdapter(adapter);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_mis_pujas);
        swipeRefresh.setColorSchemeColors(
                android.graphics.Color.parseColor("#C6A75E"),
                android.graphics.Color.parseColor("#2D5A3D")
        );
        swipeRefresh.setOnRefreshListener(this::cargarMisPujas);

        cargarMisPujas();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMisPujas();
    }

    private void cargarMisPujas() {
        RetrofitClient.getApiService().getMisPujas().enqueue(new Callback<List<MiPuja>>() {
            @Override
            public void onResponse(Call<List<MiPuja>> call, Response<List<MiPuja>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<MiPuja> pujas = response.body();
                    if (pujas.isEmpty()) {
                        mostrarEstadoVacio();
                    } else {
                        mostrarLista(pujas);
                    }
                } else {
                    mostrarEstadoVacio();
                }
            }

            @Override
            public void onFailure(Call<List<MiPuja>> call, Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Log.e("MisPujas", "Error: ", t);
                mostrarEstadoVacio();
            }
        });
    }

    private void mostrarEstadoVacio() {
        if (layoutSinParticipaciones != null) layoutSinParticipaciones.setVisibility(View.VISIBLE);
        if (scrollMisPujas != null) scrollMisPujas.setVisibility(View.GONE);
    }

    private void mostrarLista(List<MiPuja> pujas) {
        if (layoutSinParticipaciones != null) layoutSinParticipaciones.setVisibility(View.GONE);
        if (scrollMisPujas != null) scrollMisPujas.setVisibility(View.VISIBLE);
        adapter.setPujas(pujas);
    }
}
