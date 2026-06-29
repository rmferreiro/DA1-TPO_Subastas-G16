package tpo.g16.blackwood;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminMediosPagoAdapter extends RecyclerView.Adapter<AdminMediosPagoAdapter.ViewHolder> {

    private List<Map<String, Object>> items = new ArrayList<>();
    private final OnVerificarClickListener listener;

    public interface OnVerificarClickListener {
        void onVerificarClick(Long id, int position);
    }

    public AdminMediosPagoAdapter(OnVerificarClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Map<String, Object>> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_medio_pago, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);

        String tipo = getStr(item, "tipo");
        String dueno = getStr(item, "clienteNombre");

        String tipoLabel;
        if ("TARJETA_CREDITO".equals(tipo)) {
            tipoLabel = "Tarjeta de Crédito";
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_crop);
        } else if ("CUENTA_BANCARIA".equals(tipo)) {
            tipoLabel = "Cuenta Bancaria";
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_sort_by_size);
        } else if ("CHEQUE_CERTIFICADO".equals(tipo)) {
            tipoLabel = "Cheque Certificado";
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda);
        } else {
            tipoLabel = tipo != null ? tipo : "Desconocido";
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
        }

        holder.tvTipo.setText(tipoLabel);
        holder.tvDueno.setText(dueno != null ? dueno : "Usuario desconocido");
        holder.tvDetalle.setText(buildDetalle(tipo, item));

        holder.btnVerificar.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Number idNum = (Number) item.get("id");
            if (idNum != null) {
                listener.onVerificarClick(idNum.longValue(), pos);
            }
        });
    }

    private String buildDetalle(String tipo, Map<String, Object> item) {
        if ("TARJETA_CREDITO".equals(tipo)) {
            String num = getStr(item, "numeroTarjeta");
            return num != null ? "Tarjeta: " + mask(num) : "Tarjeta: ****";
        } else if ("CUENTA_BANCARIA".equals(tipo)) {
            String banco = getStr(item, "banco");
            String cuenta = getStr(item, "numeroCuenta");
            String cbu = getStr(item, "cbuSwift");
            String num = cuenta != null ? cuenta : cbu;
            String bancoLabel = banco != null ? banco + " · " : "";
            return num != null ? bancoLabel + "Cuenta: " + mask(num) : "Cuenta bancaria";
        } else if ("CHEQUE_CERTIFICADO".equals(tipo)) {
            String banco = getStr(item, "bancoEmisor");
            String num = getStr(item, "numeroCheque");
            String bancoLabel = banco != null ? banco + " · " : "";
            return num != null ? bancoLabel + "Cheque Nº " + mask(num) : "Cheque certificado";
        }
        String detalle = getStr(item, "detalle");
        return detalle != null ? detalle : "";
    }

    private String getStr(Map<String, Object> item, String key) {
        Object val = item.get(key);
        return val instanceof String ? (String) val : null;
    }

    private String mask(String value) {
        if (value == null || value.length() < 4) return value != null ? value : "****";
        return "**** " + value.substring(value.length() - 4);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTipo;
        TextView tvDueno;
        TextView tvDetalle;
        Button btnVerificar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_admin_tipo_icon);
            tvTipo = itemView.findViewById(R.id.tv_admin_tipo_mp);
            tvDueno = itemView.findViewById(R.id.tv_admin_dueno_mp);
            tvDetalle = itemView.findViewById(R.id.tv_admin_detalle_mp);
            btnVerificar = itemView.findViewById(R.id.btn_admin_verificar_mp);
        }
    }
}
