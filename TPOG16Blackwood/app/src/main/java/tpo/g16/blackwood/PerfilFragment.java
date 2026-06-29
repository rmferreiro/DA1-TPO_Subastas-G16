package tpo.g16.blackwood;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

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
    private TextView tvOfertado;
    private TextView tvPagado;
    private TextView tvTarjeta;
    private View cardMediosPago;
    private View btnAdminMediosPago;
    private View btnAdminUsuarios;
    private View cardMisMultas;
    private TextView tvMultasResumen;
    private TextView tvMultasBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre       = view.findViewById(R.id.tv_perfil_nombre);
        tvCategoria    = view.findViewById(R.id.tv_perfil_categoria);
        tvGanadas      = view.findViewById(R.id.tv_perfil_ganadas);
        tvOfertado     = view.findViewById(R.id.tv_perfil_ofertado);
        tvPagado       = view.findViewById(R.id.tv_perfil_pagado);
        tvTarjeta      = view.findViewById(R.id.tv_perfil_tarjeta);
        cardMediosPago = view.findViewById(R.id.card_medios_pago);
        btnAdminMediosPago = view.findViewById(R.id.btn_admin_medios_pago);
        btnAdminUsuarios   = view.findViewById(R.id.btn_admin_usuarios);
        cardMisMultas      = view.findViewById(R.id.card_mis_multas);
        tvMultasResumen    = view.findViewById(R.id.tv_multas_resumen);
        tvMultasBadge      = view.findViewById(R.id.tv_multas_badge);

        cardMediosPago.setOnClickListener(v -> abrirFragment(new MediosPagoFragment()));

        if (btnAdminMediosPago != null) {
            btnAdminMediosPago.setOnClickListener(v -> abrirFragment(new AdminMediosPagoFragment()));
        }

        if (btnAdminUsuarios != null) {
            btnAdminUsuarios.setOnClickListener(v -> abrirFragment(new AdminUsuariosFragment()));
        }

        if (cardMisMultas != null) {
            cardMisMultas.setOnClickListener(v -> abrirFragment(new MisMultasFragment()));
        }

        View btnCerrarSesion = view.findViewById(R.id.btn_cerrar_sesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        }

        cargarPerfil();
        cargarMetricas();
        cargarResumenMultas();

        return view;
    }

    private void abrirFragment(Fragment fragment) {
        if (getActivity() instanceof HomeActivity) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void cerrarSesion() {
        if (getContext() == null) return;
        
        // 0. Mostrar Toast informativo
        Toast.makeText(getContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        // 1. Limpiar SharedPreferences de manera segura y definitiva
        android.content.SharedPreferences prefs = getContext().getSharedPreferences(
                tpo.g16.blackwood.network.ApiConfig.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // 2. Limpiar el token estático en memoria en el RetrofitClient
        tpo.g16.blackwood.network.RetrofitClient.setAuthToken(null);

        // 3. Redirigir a LoginActivity limpiando completamente la pila de actividades (Backstack)
        android.content.Intent intent = new android.content.Intent(getContext(), tpo.g16.blackwood.login.LoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void cargarPerfil() {
        RetrofitClient.getApiService().getPerfil().enqueue(new Callback<ClienteResponse>() {
            @Override
            public void onResponse(Call<ClienteResponse> call, Response<ClienteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ClienteResponse cliente = response.body();
                    tvNombre.setText(cliente.getNombre());
                    String categoria = cliente.getCategoria();
                    if (categoria != null && !categoria.isEmpty()) {
                        String valor = categoria.substring(0, 1).toUpperCase() + categoria.substring(1).toLowerCase();
                        String text = "Categoría actual: <font color='#C6A75E'><b>" + valor + "</b></font>";
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            tvCategoria.setText(android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            tvCategoria.setText(android.text.Html.fromHtml(text));
                        }
                    } else {
                        tvCategoria.setText("Categoría actual: -");
                    }
                    if (cliente.getUltimoMedioPago() != null && !cliente.getUltimoMedioPago().isEmpty()) {
                        tvTarjeta.setText(cliente.getUltimoMedioPago());
                    } else if (cliente.isTieneMedioPagoVerificado()) {
                        tvTarjeta.setText("Verificado");
                    } else {
                        tvTarjeta.setText("Sin verificar");
                    }
                    
                    if (cliente.isEsAdmin()) {
                        if (btnAdminMediosPago != null) {
                            btnAdminMediosPago.setVisibility(View.VISIBLE);
                        }
                        if (btnAdminUsuarios != null) {
                            btnAdminUsuarios.setVisibility(View.VISIBLE);
                        }
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
                    if (tvGanadas != null && victoriasObj != null) {
                        tvGanadas.setText(String.valueOf(((Number) victoriasObj).intValue()));
                    }

                    Object ofertadoObj = metricas.get("totalOfertado");
                    if (tvOfertado != null) {
                        long ofertado = ofertadoObj instanceof Number
                                ? ((Number) ofertadoObj).longValue() : 0L;
                        tvOfertado.setText(formatMonto(ofertado));
                    }

                    Object pagadoObj = metricas.get("totalPagado");
                    if (tvPagado != null) {
                        long pagado = pagadoObj instanceof Number
                                ? ((Number) pagadoObj).longValue() : 0L;
                        tvPagado.setText(formatMonto(pagado));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("PerfilFragment", "Error métricas: ", t);
            }
        });
    }

    private String formatMonto(long monto) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        return "$ " + nf.format(monto);
    }

    /**
     * Consulta las multas pendientes para mostrar el resumen en la card y el badge
     * de alerta. Si hay multas pendientes el badge es visible con su cantidad.
     */
    private void cargarResumenMultas() {
        if (tvMultasResumen == null) return;
        tvMultasResumen.setText("Cargando...");

        RetrofitClient.getApiService().getMultasPendientes()
                .enqueue(new Callback<java.util.List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<java.util.List<Map<String, Object>>> call,
                                           Response<java.util.List<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int pendientes = response.body().size();
                            if (tvMultasResumen != null) {
                                tvMultasResumen.setText(pendientes > 0
                                        ? pendientes + " multa(s) pendiente(s) de pago"
                                        : "Sin multas pendientes");
                            }
                            if (tvMultasBadge != null) {
                                if (pendientes > 0) {
                                    tvMultasBadge.setText(String.valueOf(pendientes));
                                    tvMultasBadge.setVisibility(View.VISIBLE);
                                } else {
                                    tvMultasBadge.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            if (tvMultasResumen != null) tvMultasResumen.setText("Ver historial de multas");
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<Map<String, Object>>> call, Throwable t) {
                        if (tvMultasResumen != null) tvMultasResumen.setText("Ver historial de multas");
                    }
                });
    }
}
