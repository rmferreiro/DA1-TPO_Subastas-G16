package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoSubastasActivity extends AppCompatActivity {

    private LinearLayout containerSubastas;

    // Filtros actualmente aplicados (null = sin filtrar por ese criterio)
    private String filtroEstado = "Todas";
    private String filtroCategoria = null;
    private String filtroRematador = null;
    private LocalDate filtroFecha = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_gestion_subastas);

        containerSubastas = findViewById(R.id.container_subastas);

        // Botón nueva subasta
        findViewById(R.id.btn_nueva_subasta).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoCrearSubastaActivity.class));
        });

        // Botón filtros → abre bottom sheet y escucha qué eligió el usuario
        findViewById(R.id.btn_filtros).setOnClickListener(v -> {
            FiltrosBottomSheet filtros = new FiltrosBottomSheet();
            filtros.setFiltrosActuales(filtroEstado, filtroCategoria, filtroRematador, filtroFecha);
            filtros.setOnFiltrosAplicadosListener((estado, categoria, rematador, fecha) -> {
                filtroEstado = estado;
                filtroCategoria = categoria;
                filtroRematador = rematador;
                filtroFecha = fecha;
                renderizarSubastas();
            });
            filtros.show(getSupportFragmentManager(), "filtros");
        });

        findViewById(R.id.nav_mis_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });

        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga cada vez que volvés a esta pantalla (por ej. después de crear una subasta),
        // manteniendo los filtros que estuvieran aplicados.
        renderizarSubastas();
    }

    private void renderizarSubastas() {
        containerSubastas.removeAllViews();
        List<Subasta> subastas = aplicarFiltros(SubastaRepository.getInstance().obtenerTodas());

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Subasta subasta : subastas) {
            View card = inflater.inflate(R.layout.item_card_subasta, containerSubastas, false);

            TextView txtEstadoChip = card.findViewById(R.id.txt_estado_chip);
            TextView txtCategoria = card.findViewById(R.id.txt_categoria);
            TextView txtFechaHora = card.findViewById(R.id.txt_fecha_hora);
            TextView txtUbicacion = card.findViewById(R.id.txt_ubicacion);
            TextView txtRematador = card.findViewById(R.id.txt_rematador);
            TextView txtLotes = card.findViewById(R.id.txt_lotes);
            TextView txtEstimacion = card.findViewById(R.id.txt_estimacion);

            txtEstadoChip.setText(subasta.getEtiquetaEstado());
            txtEstadoChip.setTextColor(colorEstado(subasta.getEstado()));

            txtCategoria.setText(subasta.getCategoria().toUpperCase());
            txtCategoria.setTextColor(colorCategoria(subasta.getCategoria()));

            txtFechaHora.setText(subasta.getFechaFormateada() + " · " + subasta.getHora());
            txtUbicacion.setText(subasta.getCiudad() + " · " + subasta.getSala());
            txtRematador.setText("Rematador: " + subasta.getRematador());
            txtLotes.setText(subasta.getCantidadLotes() + " lotes");
            txtEstimacion.setText("Estimación: " + subasta.getEstimacion() + " USD");

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, EmpleadoDetalleSubastaActivity.class);
                intent.putExtra(EmpleadoDetalleSubastaActivity.SUBASTA_ID, subasta.getId());
                startActivity(intent);
            });

            containerSubastas.addView(card);
        }
    }

    /** Filtra la lista en memoria según estado / categoría / rematador elegidos en Filtros. */
    private List<Subasta> aplicarFiltros(List<Subasta> todas) {
        List<Subasta> resultado = new ArrayList<>();

        for (Subasta s : todas) {
            if (!cumpleEstado(s)) continue;
            if (filtroCategoria != null && !filtroCategoria.equalsIgnoreCase(s.getCategoria())) continue;
            if (filtroRematador != null && !filtroRematador.equalsIgnoreCase(s.getRematador())) continue;
            if (filtroFecha != null && !filtroFecha.equals(s.getFecha())) continue;
            resultado.add(s);
        }
        return resultado;
    }

    private boolean cumpleEstado(Subasta s) {
        if (filtroEstado == null || "Todas".equals(filtroEstado)) return true;
        switch (filtroEstado) {
            case "Próximas":    return s.getEstado() == Subasta.PROXIMA;
            case "En sala":     return s.getEstado() == Subasta.EN_SALA;
            case "Finalizadas": return s.getEstado() == Subasta.FINALIZADA;
            default:            return true;
        }
    }

    private int colorEstado(int estado) {
        if (estado == Subasta.EN_SALA) return 0xFF4CAF50;       // verde
        if (estado == Subasta.FINALIZADA) return 0xFF888888;    // gris
        return 0xFFC6A75E;                                      // dorado (próxima)
    }

    private int colorCategoria(String categoria) {
        switch (categoria.toLowerCase()) {
            case "platino": return 0xFF888888;
            case "oro": return 0xFFC6A75E;
            case "plata": return 0xFFA8A8A8;
            case "especial": return 0xFF2F6FAD;
            default: return 0xFF1A1A1A; // común
        }
    }
}
