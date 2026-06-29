package tpo.g16.blackwood;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

public class MisMultasAdapter extends RecyclerView.Adapter<MisMultasAdapter.ViewHolder> {

    public interface OnPagarClickListener {
        void onPagar(long multaId);
    }

    private final List<Map<String, Object>> multas;
    private final OnPagarClickListener listener;

    public MisMultasAdapter(List<Map<String, Object>> multas, OnPagarClickListener listener) {
        this.multas = multas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_multa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> multa = multas.get(position);

        // Descripción del item / subasta
        String desc = getStr(multa, "productoDesc", getStr(multa, "subastaDesc", "Multa pendiente"));
        holder.tvDescripcion.setText(desc);

        // Montos
        double montoOfertado = getDouble(multa, "montoOfertado", 0);
        double montoMulta    = getDouble(multa, "montoMulta", 0);
        holder.tvMontoOfertado.setText(String.format("$%.0f", montoOfertado));
        holder.tvMontoMulta.setText(String.format("$%.0f", montoMulta));

        // Estado
        boolean pagada = getBool(multa, "pagada", false);
        boolean derivada = getBool(multa, "derivadoJusticia", false);
        if (derivada) {
            holder.tvEstado.setText("JUDICIAL");
            holder.tvEstado.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#6B6B6B")));
        } else if (pagada) {
            holder.tvEstado.setText("PAGADA");
            holder.tvEstado.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            holder.tvEstado.setText("PENDIENTE");
            holder.tvEstado.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#8B0000")));
        }

        // Fecha límite
        String fechaLimite = getStr(multa, "fechaLimite", "-");
        holder.tvFechaLimite.setText("Vence: " + (fechaLimite.length() > 10
                ? fechaLimite.substring(0, 10) : fechaLimite));

        // Botón pagar
        if (!pagada && !derivada) {
            holder.btnPagar.setVisibility(View.VISIBLE);
            holder.btnPagar.setOnClickListener(v -> {
                Object idObj = multa.get("id");
                if (idObj != null) listener.onPagar(((Number) idObj).longValue());
            });
        } else {
            holder.btnPagar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return multas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvEstado, tvMontoOfertado, tvMontoMulta, tvFechaLimite;
        MaterialButton btnPagar;

        ViewHolder(View view) {
            super(view);
            tvDescripcion    = view.findViewById(R.id.tv_multa_descripcion);
            tvEstado         = view.findViewById(R.id.tv_multa_estado);
            tvMontoOfertado  = view.findViewById(R.id.tv_multa_monto_ofertado);
            tvMontoMulta     = view.findViewById(R.id.tv_multa_monto_multa);
            tvFechaLimite    = view.findViewById(R.id.tv_multa_fecha_limite);
            btnPagar         = view.findViewById(R.id.btn_pagar_multa);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String getStr(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return (v != null) ? v.toString() : def;
    }

    private double getDouble(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    private boolean getBool(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k);
        return (v instanceof Boolean) ? (Boolean) v : def;
    }
}
