package tpo.g16.blackwood.vendedor;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.vendedor.model.EstadoLote;
import tpo.g16.blackwood.vendedor.model.Lote;

public class LoteAdapter extends RecyclerView.Adapter<LoteAdapter.LoteViewHolder> {

    private List<Lote> lotes;

    public LoteAdapter(List<Lote> lotes) {
        this.lotes = lotes;
    }

    public void actualizarLista(List<Lote> nuevosLotes) {
        this.lotes = nuevosLotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item_lote, parent, false);
        return new LoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoteViewHolder holder, int position) {
        Lote lote = lotes.get(position);
        holder.tvNombre.setText(lote.getNombreArticulo());
        holder.tvCategoria.setText(lote.getCategoria());
        holder.tvFecha.setText(lote.getFechaCreacion());
        holder.tvEstado.setText(lote.getResumenEstado());

        // Color del badge según estado
        int bgColor;
        int textColor = Color.WHITE;
        switch (lote.getEstadoActual()) {
            case SOLICITUD_EN_PROCESO:
            case INICIO_TASACION:
            case EN_PROCESO:
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.estado_proceso);
                break;
            case SOLICITUD_APROBADA:
            case EN_SUBASTA:
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.estado_aprobado);
                break;
            case SOLICITUD_RECHAZADA:
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.estado_rechazado);
                break;
            case LOTE_COMPRADO:
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.estado_vendido);
                break;
            default:
                bgColor = Color.GRAY;
        }
        holder.tvEstado.setBackgroundColor(bgColor);
        holder.tvEstado.setTextColor(textColor);

        holder.btnDetalle.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetalleLoteActivity.class);
            intent.putExtra("lote_id", lote.getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lotes.size();
    }

    static class LoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvFecha, tvEstado;
        Button btnDetalle;

        LoteViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreLote);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaLote);
            tvFecha = itemView.findViewById(R.id.tvFechaLote);
            tvEstado = itemView.findViewById(R.id.tvEstadoLote);
            btnDetalle = itemView.findViewById(R.id.btnVerDetalle);
        }
    }
}