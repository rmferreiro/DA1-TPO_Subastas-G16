package tpo.g16.blackwood.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import tpo.g16.blackwood.R;

public class AdminArticulosAdapter extends RecyclerView.Adapter<AdminArticulosAdapter.ViewHolder> {

    private final List<Map<String, Object>> articulos;
    private final Context context;

    public AdminArticulosAdapter(Context context, List<Map<String, Object>> articulos) {
        this.context = context;
        this.articulos = articulos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mis_articulos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> articulo = articulos.get(position);

        String nombre = (String) articulo.get("descripcion");
        String subtitulo = (String) articulo.get("subtitulo");
        String estadoRaw = (String) articulo.get("estado");
        Double idDouble = (Double) articulo.get("id");
        final int id = idDouble != null ? idDouble.intValue() : 0;

        holder.txtNombre.setText(nombre != null && !nombre.isEmpty() ? nombre : "Artículo sin nombre");
        holder.txtDetalles.setText(subtitulo != null && !subtitulo.isEmpty() ? subtitulo : "Sin detalles");
        
        holder.txtEstado.setText("Estado: Pendiente revisión");

        View.OnClickListener clickListener = v -> {
            Intent intent = new Intent(context, EvaluarLoteActivity.class);
            intent.putExtra("productoId", id);
            context.startActivity(intent);
        };
        holder.itemView.setOnClickListener(clickListener);
        holder.txtVerDetalle.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return articulos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDetalles, txtEstado, txtVerDetalle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre);
            txtDetalles = itemView.findViewById(R.id.txt_detalles);
            txtEstado = itemView.findViewById(R.id.txt_estado);
            txtVerDetalle = itemView.findViewById(R.id.txt_ver_detalle);
        }
    }
}
