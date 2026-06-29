package tpo.g16.blackwood;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.api.ClienteApiService;

public class AdminUsuariosFragment extends Fragment implements AdminUsuariosAdapter.OnAprobarClickListener {

    private RecyclerView recyclerView;
    private AdminUsuariosAdapter adapter;
    private ClienteApiService apiService;
    private ProgressBar progressBar;
    private View emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_usuarios, container, false);

        recyclerView    = view.findViewById(R.id.rv_admin_usuarios);
        progressBar     = view.findViewById(R.id.pb_usuarios_loading);
        emptyStateLayout = view.findViewById(R.id.layout_usuarios_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminUsuariosAdapter(this);
        recyclerView.setAdapter(adapter);

        ImageView btnBack = view.findViewById(R.id.btn_admin_usuarios_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        apiService = RetrofitClient.getInstance(getContext()).getClienteApiService();
        cargarUsuariosPendientes();

        return view;
    }

    private void cargarUsuariosPendientes() {
        mostrarCargando();
        apiService.getUsuariosPendientes().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> lista = response.body();
                    if (lista.isEmpty()) {
                        mostrarVacio();
                    } else {
                        adapter.setItems(lista);
                        mostrarLista();
                    }
                } else {
                    ocultarCargando();
                    Toast.makeText(getContext(),
                            "Error al cargar usuarios (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("AdminUsuariosFragment", "Error de red", t);
                ocultarCargando();
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAprobarClick(String uuid, String categoria, int position) {
        Map<String, Object> body = new HashMap<>();
        body.put("categoria", categoria);

        apiService.aprobarUsuario(uuid, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    String nombreCategoria = categoria.substring(0, 1).toUpperCase() + categoria.substring(1);
                    Toast.makeText(getContext(),
                            "Usuario aprobado como " + nombreCategoria,
                            Toast.LENGTH_SHORT).show();
                    adapter.removeItem(position);
                    if (adapter.getItemCount() == 0) {
                        mostrarVacio();
                    }
                } else {
                    // Reactivar el botón para que el admin pueda reintentar
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(),
                            "Error al aprobar (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("AdminUsuariosFragment", "Error de red al aprobar", t);
                adapter.notifyItemChanged(position);
                Toast.makeText(getContext(), "Error de red al aprobar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCargando() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
    }

    private void ocultarCargando() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void mostrarLista() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
    }

    private void mostrarVacio() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
    }
}
