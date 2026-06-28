package tpo.g16.blackwood;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import tpo.g16.blackwood.network.models.MiPuja;

public class MisPujasAdapter extends RecyclerView.Adapter<MisPujasAdapter.ViewHolder> {

    private final Context context;
    private List<MiPuja> pujas = new ArrayList<>();

    public MisPujasAdapter(Context context) {
        this.context = context;
    }

    public void setPujas(List<MiPuja> pujas) {
        this.pujas = pujas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mi_puja, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MiPuja puja = pujas.get(position);

        holder.tvProductoDesc.setText(puja.getProductoDesc());
        holder.tvSubastaDesc.setText(puja.getSubastaDesc());
        holder.tvMiPuja.setText(String.format("Mi puja máxima: $%,.0f", puja.getMiPuja()));

        String estado = puja.getEstado();
        if ("GANANDO".equals(estado)) {
            holder.tvBadgeEstado.setText("● En vivo");
            holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#1C2A21"));
            holder.tvBadgeEstado.setTextColor(Color.parseColor("#C6A75E"));
            
            holder.tvResultadoEstado.setText("Lote adjudicado (Ganando)");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#1C2A21"));
            holder.tvMiPuja.setTextColor(Color.parseColor("#C6A75E"));
            
        } else if ("PERDIENDO".equals(estado)) {
            holder.tvBadgeEstado.setText("● En vivo");
            holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#EAE6DF"));
            holder.tvBadgeEstado.setTextColor(Color.parseColor("#6B6B6B"));
            
            holder.tvResultadoEstado.setText("No ganando");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#6B6B6B"));
            holder.tvMiPuja.setTextColor(Color.parseColor("#6B6B6B"));
            
        } else if ("GANADA".equals(estado)) {
            holder.tvBadgeEstado.setText("★ Ganada");
            holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#1C2A21"));
            holder.tvBadgeEstado.setTextColor(Color.parseColor("#C6A75E"));
            
            holder.tvResultadoEstado.setText("¡Ganaste!");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#1C2A21"));
            holder.tvMiPuja.setTextColor(Color.parseColor("#C6A75E"));
            
        } else if ("PERDIDA".equals(estado)) {
            holder.tvBadgeEstado.setText("Finalizada");
            holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#EAE6DF"));
            holder.tvBadgeEstado.setTextColor(Color.parseColor("#6B6B6B"));
            
            holder.tvResultadoEstado.setText("No ganado");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#6B6B6B"));
            holder.tvMiPuja.setTextColor(Color.parseColor("#6B6B6B"));
        } else if ("PAGADA".equals(estado)) {
            holder.tvBadgeEstado.setText("★ Pagada");
            holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#1C2A21"));
            holder.tvBadgeEstado.setTextColor(Color.parseColor("#C6A75E"));
            
            holder.tvResultadoEstado.setText("Pago Confirmado");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#1C2A21"));
            holder.tvMiPuja.setTextColor(Color.parseColor("#C6A75E"));
            holder.btnPagarItem.setVisibility(View.GONE);
        }

        if ("GANADA".equals(estado)) {
            holder.btnPagarItem.setVisibility(View.VISIBLE);
            holder.btnPagarItem.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, ConfirmarPagoActivity.class);
                intent.putExtra("ITEM_ID", puja.getItemId());
                intent.putExtra("OFERTA", puja.getMiPuja());
                intent.putExtra("DESCRIPCION", puja.getProductoDesc());
                context.startActivity(intent);
            });
        } else {
            holder.btnPagarItem.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if ("GANADA".equals(estado) || "PAGADA".equals(estado)) {
                android.content.Intent intent = new android.content.Intent(context, NotificacionGanadorActivity.class);
                intent.putExtra("productoDesc", puja.getProductoDesc());
                intent.putExtra("subastaDesc", puja.getSubastaDesc());
                intent.putExtra("precio", puja.getMiPuja());
                context.startActivity(intent);
            } else if ("PERDIDA".equals(estado)) {
                android.content.Intent intent = new android.content.Intent(context, NotificacionPerdedorActivity.class);
                intent.putExtra("productoDesc", puja.getProductoDesc());
                intent.putExtra("subastaDesc", puja.getSubastaDesc());
                intent.putExtra("precio", puja.getMiPuja());
                context.startActivity(intent);
            } else {
                android.content.Intent intent = new android.content.Intent(context, SubastaEnVivoActivity.class);
                intent.putExtra("SUBASTA_ID", puja.getSubastaId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pujas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductoDesc, tvBadgeEstado, tvSubastaDesc, tvMiPuja, tvResultadoEstado, btnPagarItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductoDesc = itemView.findViewById(R.id.tv_producto_desc);
            tvBadgeEstado = itemView.findViewById(R.id.tv_badge_estado);
            tvSubastaDesc = itemView.findViewById(R.id.tv_subasta_desc);
            tvMiPuja = itemView.findViewById(R.id.tv_mi_puja);
            tvResultadoEstado = itemView.findViewById(R.id.tv_resultado_estado);
            btnPagarItem = itemView.findViewById(R.id.btn_pagar_item);
        }
    }
}
