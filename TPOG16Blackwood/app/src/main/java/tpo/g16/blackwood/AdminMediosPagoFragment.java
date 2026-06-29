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

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.api.MedioPagoApiService;

public class AdminMediosPagoFragment extends Fragment implements AdminMediosPagoAdapter.OnVerificarClickListener {

    private RecyclerView recyclerView;
    private AdminMediosPagoAdapter adapter;
    private MedioPagoApiService apiService;
    private ProgressBar progressBar;
    private View emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_medios_pago, container, false);

        recyclerView = view.findViewById(R.id.rv_admin_medios_pago);
        progressBar = view.findViewById(R.id.pb_admin_loading);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminMediosPagoAdapter(this);
        recyclerView.setAdapter(adapter);

        ImageView btnBack = view.findViewById(R.id.btn_admin_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        apiService = RetrofitClient.getInstance(getContext()).getMedioPagoApiService();
        cargarMediosPago();

        return view;
    }

    private void cargarMediosPago() {
        mostrarCargando();
        apiService.getMediosPagoNoVerificados().enqueue(new Callback<List<Map<String, Object>>>() {
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
                    Toast.makeText(getContext(), "Error al cargar medios de pago (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("AdminMPFragment", "Error de red", t);
                ocultarCargando();
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVerificarClick(Long id, int position) {
        apiService.verificarMedioPago(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Medio de pago verificado", Toast.LENGTH_SHORT).show();
                    adapter.removeItem(position);
                    if (adapter.getItemCount() == 0) {
                        mostrarVacio();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al verificar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("AdminMPFragment", "Error de red al verificar", t);
                Toast.makeText(getContext(), "Error de red al verificar", Toast.LENGTH_SHORT).show();
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
