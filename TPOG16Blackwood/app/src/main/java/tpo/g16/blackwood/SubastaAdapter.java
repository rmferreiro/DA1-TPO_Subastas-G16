package tpo.g16.blackwood;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tpo.g16.blackwood.network.models.SubastaResponse;

public class SubastaAdapter extends RecyclerView.Adapter<SubastaAdapter.SubastaViewHolder> {

    private List<SubastaResponse> subastasList;

    public SubastaAdapter(List<SubastaResponse> subastasList) {
        this.subastasList = subastasList;
    }

    @NonNull
    @Override
    public SubastaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subasta, parent, false);
        return new SubastaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubastaViewHolder holder, int position) {
        SubastaResponse subasta = subastasList.get(position);

        // Map values
        holder.tvCategoria.setText(subasta.getCategoria() != null ? subasta.getCategoria().toUpperCase() : "GENERAL");
        
        // Formatear Fecha y Hora
        String fechaHora = (subasta.getFecha() != null ? subasta.getFecha() : "") + " · " + (subasta.getHora() != null ? subasta.getHora() : "");
        holder.tvFechaHora.setText(fechaHora);
        
        // Ubicacion y Rematador
        holder.tvUbicacion.setText(subasta.getUbicacion() != null ? subasta.getUbicacion() : "Desconocido");
        holder.tvRematador.setText("Rematador: " + (subasta.getSubastadorNombre() != null ? subasta.getSubastadorNombre() : "Pendiente"));
        
        // Moneda (reemplaza Capacidad)
        String moneda = subasta.getMoneda() != null ? subasta.getMoneda().toUpperCase() : "ARS";
        holder.tvMoneda.setText("Moneda: " + moneda);
        
        // Descripción / Estimación
        holder.tvDescripcion.setText(subasta.getDescripcion() != null ? subasta.getDescripcion() : "Sin descripción");

        // Estado: Pendiente (azul) | Activa (verde) | Finalizada (gris)
        String estado = subasta.getEstado() != null ? subasta.getEstado().toUpperCase() : "";
        if ("PENDIENTE".equals(estado)) {
            holder.tvEstado.setText("Pendiente");
            int azul = Color.parseColor("#1565C0"); // Azul fuerte
            holder.tvEstado.setTextColor(azul);
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(azul));
        } else if ("ACTIVA".equals(estado)) {
            holder.tvEstado.setText("Activa");
            int verde = Color.parseColor("#1B7A3E"); // Verde fuerte
            holder.tvEstado.setTextColor(verde);
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(verde));
        } else if ("FINALIZADA".equals(estado)) {
            holder.tvEstado.setText("Finalizada");
            int gris = Color.parseColor("#757575"); // Gris
            holder.tvEstado.setTextColor(gris);
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(gris));
        } else {
            holder.tvEstado.setText(subasta.getEstado());
            int gris = Color.parseColor("#A7A9AC");
            holder.tvEstado.setTextColor(gris);
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(gris));
        }

        // Categoría Color
        String cat = subasta.getCategoria() != null ? subasta.getCategoria().toUpperCase() : "";
        if ("ORO".equals(cat)) {
            holder.tvCategoria.setTextColor(Color.parseColor("#C6A75E"));
        } else if ("PLATINO".equals(cat)) {
            holder.tvCategoria.setTextColor(Color.parseColor("#808080")); // Platino / Gris oscuro
        } else if ("PLATA".equals(cat)) {
            holder.tvCategoria.setTextColor(Color.parseColor("#A0A0A0")); // Plata
        } else if ("ESPECIAL".equals(cat)) {
            holder.tvCategoria.setTextColor(Color.parseColor("#4A90E2")); // Especial / Azul
        } else {
            holder.tvCategoria.setTextColor(Color.parseColor("#6B6B6B")); // Comun
        }

        // Clic en la tarjeta
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalleSubastaActivity.class);
            intent.putExtra("SUBASTA_ID", subasta.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subastasList == null ? 0 : subastasList.size();
    }

    public void updateData(List<SubastaResponse> nuevasSubastas) {
        this.subastasList = nuevasSubastas;
        notifyDataSetChanged();
    }

    static class SubastaViewHolder extends RecyclerView.ViewHolder {
        View dotEstado;
        TextView tvEstado, tvCategoria, tvFechaHora, tvUbicacion, tvRematador, tvMoneda, tvDescripcion;

        public SubastaViewHolder(@NonNull View itemView) {
            super(itemView);
            dotEstado = itemView.findViewById(R.id.dot_estado);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvCategoria = itemView.findViewById(R.id.tv_categoria);
            tvFechaHora = itemView.findViewById(R.id.tv_fecha_hora);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion);
            tvRematador = itemView.findViewById(R.id.tv_rematador);
            tvMoneda = itemView.findViewById(R.id.tv_moneda);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
        }
    }
}
