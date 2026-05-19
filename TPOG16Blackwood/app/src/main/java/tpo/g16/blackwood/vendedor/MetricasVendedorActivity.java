package tpo.g16.blackwood.vendedor;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.MetricasVendedor;
import tpo.g16.blackwood.vendedor.repository.LoteRepository;

public class MetricasVendedorActivity extends AppCompatActivity {

    private TextView tvPublicados, tvVendidos, tvRechazados, tvTotalGenerado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metricas_vendedor);

        tvPublicados = findViewById(R.id.tvLotesPublicados);
        tvVendidos = findViewById(R.id.tvLotesVendidos);
        tvRechazados = findViewById(R.id.tvLotesRechazados);
        tvTotalGenerado = findViewById(R.id.tvTotalGenerado);

        MetricasVendedor metricas = LoteRepository.getInstance().getMetricas();

        tvPublicados.setText(String.valueOf(metricas.getLotesPublicados()));
        tvVendidos.setText(String.valueOf(metricas.getLotesVendidos()));
        tvRechazados.setText(String.valueOf(metricas.getLotesRechazados()));
        tvTotalGenerado.setText("$" + String.format("%.2f", metricas.getTotalGenerado()));
    }
}