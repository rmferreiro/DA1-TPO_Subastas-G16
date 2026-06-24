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
import tpo.g16.blackwood.main.HomeActivity;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.models.ClienteResponse;

public class PerfilFragment extends Fragment {

    private TextView tvNombre;
    private TextView tvCategoria;
    private TextView tvGanadas;
    private TextView tvParticipaciones;
    private TextView tvTarjeta;
    private TextView tvOfertadoPagado;
    private View cardParticipaciones;
    private View cardHistorial;
    private View cardMediosPago;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre = view.findViewById(R.id.tv_perfil_nombre);
        tvCategoria = view.findViewById(R.id.tv_perfil_categoria);
        tvGanadas = view.findViewById(R.id.tv_perfil_ganadas);
        tvParticipaciones = view.findViewById(R.id.tv_perfil_participaciones);
        tvTarjeta = view.findViewById(R.id.tv_perfil_tarjeta);
        tvOfertadoPagado = view.findViewById(R.id.tv_perfil_ofertado_pagado);
        
        cardParticipaciones = view.findViewById(R.id.card_participaciones);
        cardHistorial = view.findViewById(R.id.card_historial);
        cardMediosPago = view.findViewById(R.id.card_medios_pago);

        cardParticipaciones.setOnClickListener(v -> {
            if (getActivity() != null && getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).selectTab(1); // Va a "Mis Pujas"
            }
        });

        cardHistorial.setOnClickListener(v -> {
            if (getActivity() != null && getActivity() instanceof HomeActivity) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HistorialFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        cardMediosPago.setOnClickListener(v -> {
            if (getActivity() != null && getActivity() instanceof HomeActivity) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MediosPagoFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        cargarPerfil();
        cargarMetricas();

        return view;
    }

    private void cargarPerfil() {
        RetrofitClient.getApiService().getPerfil().enqueue(new Callback<ClienteResponse>() {
            @Override
            public void onResponse(Call<ClienteResponse> call, Response<ClienteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ClienteResponse cliente = response.body();
                    tvNombre.setText(cliente.getNombre());
                    tvCategoria.setText(cliente.getCategoria());
                    if (cliente.getUltimoMedioPago() != null && !cliente.getUltimoMedioPago().isEmpty()) {
                        tvTarjeta.setText(cliente.getUltimoMedioPago());
                    } else if (cliente.isTieneMedioPagoVerificado()) {
                        tvTarjeta.setText("Verificado");
                    } else {
                        tvTarjeta.setText("Sin verificar");
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ClienteResponse> call, Throwable t) {
                Log.e("PerfilFragment", "Error perfil: ", t);
            }
        });
    }

    private void cargarMetricas() {
        RetrofitClient.getApiService().getMetricas().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> metricas = response.body();
                    
                    Object victoriasObj = metricas.get("totalVictorias");
                    Object pujasObj = metricas.get("totalPujas");
                    Object ofertadoObj = metricas.get("totalOfertado");
                    Object pagadoObj = metricas.get("totalPagado");
                    
                    if (victoriasObj != null) {
                        tvGanadas.setText(String.valueOf(((Number) victoriasObj).intValue()));
                    }
                    if (pujasObj != null) {
                        int pujas = ((Number) pujasObj).intValue();
                        tvParticipaciones.setText(pujas + " finalizadas");
                    }
                    
                    int ofertado = ofertadoObj != null ? ((Number) ofertadoObj).intValue() : 0;
                    int pagado = pagadoObj != null ? ((Number) pagadoObj).intValue() : 0;
                    tvOfertadoPagado.setText("$" + ofertado + " / $" + pagado);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("PerfilFragment", "Error métricas: ", t);
            }
        });
    }
}
