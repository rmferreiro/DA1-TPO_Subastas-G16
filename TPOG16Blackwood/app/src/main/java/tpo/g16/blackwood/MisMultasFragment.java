package tpo.g16.blackwood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;

/**
 * Pantalla de multas del usuario: muestra todas las multas (pagas y pendientes)
 * y permite pagar las pendientes directamente.
 *
 * Accesible desde PerfilFragment → card "Mis multas".
 */
public class MisMultasFragment extends Fragment {

    private RecyclerView rvMultas;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout bannerPendientes;
    private LinearLayout layoutVacias;

    private MisMultasAdapter adapter;
    private final List<Map<String, Object>> multas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_multas, container, false);

        rvMultas          = view.findViewById(R.id.rv_multas);
        swipeRefresh      = view.findViewById(R.id.swipe_multas);
        bannerPendientes  = view.findViewById(R.id.banner_multas_pendientes);
        layoutVacias      = view.findViewById(R.id.layout_multas_vacias);

        // Botón volver
        view.findViewById(R.id.btn_back_multas).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // RecyclerView
        rvMultas.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MisMultasAdapter(multas, this::pagarMulta);
        rvMultas.setAdapter(adapter);

        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener(this::cargarMultas);

        cargarMultas();
        return view;
    }

    private void cargarMultas() {
        swipeRefresh.setRefreshing(true);
        RetrofitClient.getApiService().getTodasMultas()
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call,
                                           Response<List<Map<String, Object>>> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            actualizarLista(response.body());
                        } else {
                            mostrarError("Error al cargar multas (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        mostrarError("Error de red: " + t.getMessage());
                    }
                });
    }

    private void actualizarLista(List<Map<String, Object>> nuevasMultas) {
        multas.clear();
        multas.addAll(nuevasMultas);
        adapter.notifyDataSetChanged();

        boolean hayPendientes = multas.stream()
                .anyMatch(m -> {
                    Object pagada = m.get("pagada");
                    return !(pagada instanceof Boolean && (Boolean) pagada);
                });

        if (multas.isEmpty()) {
            swipeRefresh.setVisibility(View.GONE);
            layoutVacias.setVisibility(View.VISIBLE);
            bannerPendientes.setVisibility(View.GONE);
        } else {
            swipeRefresh.setVisibility(View.VISIBLE);
            layoutVacias.setVisibility(View.GONE);
            bannerPendientes.setVisibility(hayPendientes ? View.VISIBLE : View.GONE);
        }
    }

    private void pagarMulta(long multaId) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Pagar multa")
                .setMessage("¿Confirmás el pago de esta multa? Se debitará de tu medio de pago registrado.")
                .setPositiveButton("Confirmar", (dialog, which) -> ejecutarPago(multaId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarPago(long multaId) {
        RetrofitClient.getApiService().pagarMulta(multaId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Multa pagada exitosamente", Toast.LENGTH_SHORT).show();
                            cargarMultas(); // Refrescar la lista
                        } else {
                            mostrarError("No se pudo procesar el pago (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        mostrarError("Error de red: " + t.getMessage());
                    }
                });
    }

    private void mostrarError(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
