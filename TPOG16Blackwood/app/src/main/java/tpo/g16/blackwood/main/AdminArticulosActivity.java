package tpo.g16.blackwood.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class AdminArticulosActivity extends AppCompatActivity {

    private RecyclerView rvArticulos;
    private TextView txtEstadoVacio;
    private ProgressBar progressBar;
    private List<Map<String, Object>> articulosList = new ArrayList<>();
    private AdminArticulosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_articulos);

        // Configurar título del Header
        TextView headerSubtitle = findViewById(R.id.header_subtitle);
        if (headerSubtitle != null) {
            headerSubtitle.setText("Artículos pendientes");
        }

        rvArticulos = findViewById(R.id.rv_articulos_pendientes);
        txtEstadoVacio = findViewById(R.id.txt_estado_vacio);
        progressBar = findViewById(R.id.progress_bar);

        rvArticulos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminArticulosAdapter(this, articulosList);
        rvArticulos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPendientes();
    }

    private void cargarPendientes() {
        progressBar.setVisibility(View.VISIBLE);
        txtEstadoVacio.setVisibility(View.GONE);

        RetrofitClient.getApiService().getProductosPendientes()
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
                            Toast.makeText(AdminArticulosActivity.this, "Error al cargar pendientes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        txtEstadoVacio.setVisibility(View.VISIBLE);
                        Toast.makeText(AdminArticulosActivity.this, "Falla al conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
