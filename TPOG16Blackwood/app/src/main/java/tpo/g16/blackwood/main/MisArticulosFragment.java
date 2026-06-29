package tpo.g16.blackwood.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.R;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.register.NuevoLoteActivity;

public class MisArticulosFragment extends Fragment {

    private RecyclerView rvMisArticulos;
    private TextView txtEstadoVacio;
    private ProgressBar progressBar;
    private Button btnNuevoArticulo, btnAdministrarArticulos;
    private List<Map<String, Object>> articulosList = new ArrayList<>();
    private MisArticulosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_articulos, container, false);

        rvMisArticulos = view.findViewById(R.id.rv_mis_articulos);
        txtEstadoVacio = view.findViewById(R.id.txt_estado_vacio);
        progressBar = view.findViewById(R.id.progress_bar);
        btnNuevoArticulo = view.findViewById(R.id.btn_nuevo_articulo);
        btnAdministrarArticulos = view.findViewById(R.id.btn_administrar_articulos);

        rvMisArticulos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MisArticulosAdapter(getContext(), articulosList);
        rvMisArticulos.setAdapter(adapter);

        btnNuevoArticulo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NuevoLoteActivity.class);
            startActivity(intent);
        });

        verificarPerfilAdmin();

        return view;
    }

    private void verificarPerfilAdmin() {
        RetrofitClient.getApiService().getPerfil()
                .enqueue(new Callback<tpo.g16.blackwood.network.models.ClienteResponse>() {
                    @Override
                    public void onResponse(Call<tpo.g16.blackwood.network.models.ClienteResponse> call, Response<tpo.g16.blackwood.network.models.ClienteResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isEsAdmin()) {
                                btnAdministrarArticulos.setVisibility(View.VISIBLE);
                                btnAdministrarArticulos.setOnClickListener(v -> {
                                    Intent intent = new Intent(getActivity(), AdminArticulosActivity.class);
                                    startActivity(intent);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<tpo.g16.blackwood.network.models.ClienteResponse> call, Throwable t) {}
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarArticulos();
    }

    private void cargarArticulos() {
        progressBar.setVisibility(View.VISIBLE);
        txtEstadoVacio.setVisibility(View.GONE);

        RetrofitClient.getApiService()
                .getMisProductos()
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            articulosList.clear();
                            articulosList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            if (articulosList.isEmpty()) {
                                txtEstadoVacio.setVisibility(View.VISIBLE);
                            }
                        } else {
                            txtEstadoVacio.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Error al cargar artículos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        txtEstadoVacio.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Falla de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
