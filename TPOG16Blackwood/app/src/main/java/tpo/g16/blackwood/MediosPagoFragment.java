package tpo.g16.blackwood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import tpo.g16.blackwood.network.RetrofitClient;

public class MediosPagoFragment extends Fragment {

    private RecyclerView recyclerMediosPago;
    private MedioPagoAdapter adapter;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medios_pago, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        View btnAddCard = view.findViewById(R.id.btn_add_payment_card);
        if (btnAddCard != null) {
            btnAddCard.setOnClickListener(v -> {
                AddPaymentMethodFragment addFragment = new AddPaymentMethodFragment();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, addFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        recyclerMediosPago = view.findViewById(R.id.recycler_medios_pago);
        recyclerMediosPago.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MedioPagoAdapter(new java.util.ArrayList<>(), new MedioPagoAdapter.OnMedioPagoActionListener() {
            @Override
            public void onEdit(Map<String, Object> medioPago, int position) {
                // Ir a editar (le pasamos los datos o al menos permitimos pisar)
                // Usando el fragmento existente
                AddPaymentMethodFragment addFragment = new AddPaymentMethodFragment();
                Bundle args = new Bundle();
                
                String tipo = (String) medioPago.get("tipo");
                if ("TARJETA_CREDITO".equals(tipo)) args.putString("edit_tipo", "Tarjeta de crédito");
                else if ("CUENTA_BANCARIA".equals(tipo)) args.putString("edit_tipo", "Cuenta bancaria");
                else args.putString("edit_tipo", "Cheque certificado");
                
                addFragment.setArguments(args);
                
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, addFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }

            @Override
            public void onDelete(Map<String, Object> medioPago, int position) {
                if (medioPago.containsKey("id")) {
                    Long id = ((Number) medioPago.get("id")).longValue();
                    RetrofitClient.getApiService().eliminarMedioPago(id).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Medio de pago eliminado", Toast.LENGTH_SHORT).show();
                                cargarMediosPago();
                            } else {
                                Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        recyclerMediosPago.setAdapter(adapter);

        cargarMediosPago();

        return view;
    }

    private void cargarMediosPago() {
        RetrofitClient.getApiService().getMediosPago().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(getContext(), "Error al cargar medios de pago", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getParentFragmentManager().setFragmentResultListener("add_payment_request", this, (requestKey, bundle) -> {
            String tipo = bundle.getString("tipo");
            String detalle = bundle.getString("detalle");

            // Preparar el JSON para POST /api/medios-pago
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("moneda", "ARS");

            if ("Tarjeta de crédito".equals(tipo)) {
                request.put("tipo", "TARJETA_CREDITO");
                String[] parts = detalle.split("; ");
                request.put("numeroTarjeta", parts.length > 0 ? parts[0] : "0000000000000000");
                request.put("titular", parts.length > 1 ? parts[1] : "Desconocido");
                request.put("vencimiento", parts.length > 2 ? parts[2] : "12/99");
                request.put("esTarjetaInternacional", false);
            } else if ("Cuenta bancaria".equals(tipo)) {
                request.put("tipo", "CUENTA_BANCARIA");
                String[] parts = detalle.split("; ");
                request.put("banco", parts.length > 0 ? parts[0] : "Banco X");
                request.put("numeroCuenta", parts.length > 1 ? parts[1] : "000000");
                request.put("cbuSwift", parts.length > 2 ? parts[2] : "000000000000");
                request.put("esInternacional", false);
            } else {
                request.put("tipo", "CHEQUE_CERTIFICADO");
                String[] parts = detalle.split("; ");
                request.put("bancoEmisor", parts.length > 0 ? parts[0] : "Banco X");
                request.put("numeroCheque", parts.length > 1 ? parts[1] : "0000");
                request.put("montoCertificado", 0);
            }

            RetrofitClient.getApiService().agregarMedioPago(request).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Medio de pago agregado correctamente", Toast.LENGTH_SHORT).show();
                        cargarMediosPago();
                    } else {
                        Toast.makeText(getContext(), "Error al agregar medio de pago", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
