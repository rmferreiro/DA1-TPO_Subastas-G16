package tpo.g16.blackwood;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MedioPagoAdapter extends RecyclerView.Adapter<MedioPagoAdapter.MedioPagoViewHolder> {

    private List<Map<String, Object>> mediosPagoList;
    private OnMedioPagoActionListener listener;

    public interface OnMedioPagoActionListener {
        void onEdit(Map<String, Object> medioPago, int position);
        void onDelete(Map<String, Object> medioPago, int position);
    }

    public MedioPagoAdapter(List<Map<String, Object>> mediosPagoList, OnMedioPagoActionListener listener) {
        this.mediosPagoList = mediosPagoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedioPagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medio_pago, parent, false);
        return new MedioPagoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedioPagoViewHolder holder, int position) {
        Map<String, Object> medioPago = mediosPagoList.get(position);

        String tipoStr = "";
        String detalle = "";

        // Parseo seguro del JSON
        if (medioPago.containsKey("tipo")) {
            String tipo = (String) medioPago.get("tipo");
            if ("TARJETA_CREDITO".equals(tipo)) {
                tipoStr = "Tarjeta de Crédito";
                detalle = "Terminada en " + mask((String) medioPago.get("numeroTarjeta"));
                holder.ivTipo.setImageResource(android.R.drawable.ic_menu_crop);
            } else if ("CUENTA_BANCARIA".equals(tipo)) {
                tipoStr = "Cuenta Bancaria";
                detalle = "CBU: " + mask((String) medioPago.get("cbuSwift"));
                holder.ivTipo.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            } else if ("CHEQUE_CERTIFICADO".equals(tipo)) {
                tipoStr = "Cheque Certificado";
                detalle = "Cheque Nº " + mask((String) medioPago.get("numeroCheque"));
                holder.ivTipo.setImageResource(android.R.drawable.ic_menu_agenda);
            }
        }

        holder.tvTipo.setText(tipoStr);
        holder.tvDetalle.setText(detalle);

        Boolean verificado = medioPago.containsKey("verificado") ? (Boolean) medioPago.get("verificado") : false;
        
        if (verificado != null && verificado) {
            holder.tvEstado.setText("Verificado");
            holder.tvEstado.setTextColor(Color.parseColor("#5C7A65")); // Verde
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F0EA")));
        } else {
            holder.tvEstado.setText("Pendiente");
            holder.tvEstado.setTextColor(Color.parseColor("#C6A75E")); // Dorado
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FBF6EB")));
        }

        // Si hay más de un medio de pago, permitir eliminar. Si hay 1 o menos, ocultar eliminar.
        // Editar se puede siempre.
        if (holder.layoutAcciones != null) {
            holder.layoutAcciones.setVisibility(View.VISIBLE);
        }
        
        if (holder.btnDelete != null) {
            if (mediosPagoList.size() > 1) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(medioPago, position);
                });
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }
        }

        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(medioPago, position);
            });
        }
    }

    private String mask(String value) {
        if (value == null || value.length() < 4) return value != null ? value : "";
        return "**** " + value.substring(value.length() - 4);
    }

    @Override
    public int getItemCount() {
        return mediosPagoList == null ? 0 : mediosPagoList.size();
    }

    public void updateData(List<Map<String, Object>> nuevaLista) {
        this.mediosPagoList = nuevaLista;
        notifyDataSetChanged();
    }

    static class MedioPagoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTipo;
        TextView tvTipo;
        TextView tvDetalle;
        TextView tvEstado;
        View layoutAcciones;
        View btnEdit;
        View btnDelete;

        public MedioPagoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTipo = itemView.findViewById(R.id.iv_tipo_pago);
            tvTipo = itemView.findViewById(R.id.tv_tipo_pago);
            tvDetalle = itemView.findViewById(R.id.tv_detalle_pago);
            tvEstado = itemView.findViewById(R.id.tv_estado_pago);
            layoutAcciones = itemView.findViewById(R.id.layout_acciones);
            btnEdit = itemView.findViewById(R.id.btn_edit_pago);
            btnDelete = itemView.findViewById(R.id.btn_delete_pago);
        }
    }
}
