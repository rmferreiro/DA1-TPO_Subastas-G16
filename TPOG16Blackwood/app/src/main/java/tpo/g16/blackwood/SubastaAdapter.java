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
        
        // Capacidad / Asistentes (aproximado a Lotes para encajar en el diseño anterior)
        holder.tvCapacidad.setText("Capacidad: " + subasta.getCapacidadAsistentes());
        
        // Descripción / Estimación
        holder.tvDescripcion.setText(subasta.getDescripcion() != null ? subasta.getDescripcion() : "Sin descripción");

        // Estado
        if ("ABIERTA".equalsIgnoreCase(subasta.getEstado()) || "EN_CURSO".equalsIgnoreCase(subasta.getEstado())) {
            holder.tvEstado.setText("En sala");
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#5C7A65"))); // Verde
        } else if ("PROGRAMADA".equalsIgnoreCase(subasta.getEstado())) {
            holder.tvEstado.setText("Próxima sesión");
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6E2C2C"))); // Rojo oscuro
        } else {
            holder.tvEstado.setText(subasta.getEstado());
            holder.dotEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#A7A9AC"))); // Gris
        }

        // Categoría Color
        if ("ORO".equalsIgnoreCase(subasta.getCategoria())) {
            holder.tvCategoria.setTextColor(Color.parseColor("#C6A75E"));
        } else if ("PLATINO".equalsIgnoreCase(subasta.getCategoria())) {
            holder.tvCategoria.setTextColor(Color.parseColor("#A7A9AC"));
        } else if ("DIAMANTE".equalsIgnoreCase(subasta.getCategoria())) {
            holder.tvCategoria.setTextColor(Color.parseColor("#2C3E50"));
        } else {
            holder.tvCategoria.setTextColor(Color.parseColor("#6B6B6B"));
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
        TextView tvEstado, tvCategoria, tvFechaHora, tvUbicacion, tvRematador, tvCapacidad, tvDescripcion;

        public SubastaViewHolder(@NonNull View itemView) {
            super(itemView);
            dotEstado = itemView.findViewById(R.id.dot_estado);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvCategoria = itemView.findViewById(R.id.tv_categoria);
            tvFechaHora = itemView.findViewById(R.id.tv_fecha_hora);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion);
            tvRematador = itemView.findViewById(R.id.tv_rematador);
            tvCapacidad = itemView.findViewById(R.id.tv_capacidad);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
        }
    }
}
