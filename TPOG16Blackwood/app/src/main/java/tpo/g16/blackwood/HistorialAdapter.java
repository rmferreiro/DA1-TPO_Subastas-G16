package tpo.g16.blackwood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tpo.g16.blackwood.network.models.MiPuja;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private Context context;
    private List<MiPuja> pujas = new ArrayList<>();

    public HistorialAdapter(Context context) {
        this.context = context;
    }

    public void setPujas(List<MiPuja> pujas) {
        this.pujas = pujas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        MiPuja puja = pujas.get(position);
        
        holder.tvTitulo.setText(puja.getProductoDesc());
        
        String estadoVisual = "En curso";
        if ("si".equalsIgnoreCase(puja.getSubastado())) {
            if ("GANANDO".equalsIgnoreCase(puja.getEstado())) {
                estadoVisual = "Ganada";
            } else {
                estadoVisual = "Perdida";
            }
        }
        
        holder.tvSubtitulo.setText(estadoVisual + " · $" + (int) puja.getMiPuja());
    }

    @Override
    public int getItemCount() {
        return pujas.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvSubtitulo;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_historial_titulo);
            tvSubtitulo = itemView.findViewById(R.id.tv_historial_subtitulo);
        }
    }
}
