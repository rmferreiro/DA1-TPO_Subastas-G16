package tpo.g16.blackwood;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
        String estado = puja.getEstado();

        holder.tvProductoDesc.setText(puja.getProductoDesc());
        holder.tvSubastaDesc.setText(puja.getSubastaDesc());

        // Poblar dinámicamente la lista de ofertas
        holder.llBidsContainer.removeAllViews();
        List<Double> todasPujas = puja.getTodasMisPujas();
        if (todasPujas != null && !todasPujas.isEmpty()) {
            for (Double monto : todasPujas) {
                TextView tvOferta = new TextView(context);
                tvOferta.setText(String.format("Ofertaste $%,.0f", monto));
                tvOferta.setTextColor(Color.parseColor("#C6A75E"));
                tvOferta.setTextSize(13f);
                tvOferta.setTypeface(null, Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 4);
                tvOferta.setLayoutParams(params);
                holder.llBidsContainer.addView(tvOferta);
            }
        } else {
            // Fallback: mostrar miPuja si no hay lista detallada
            TextView tvOferta = new TextView(context);
            tvOferta.setText(String.format("Ofertaste $%,.0f", puja.getMiPuja()));
            tvOferta.setTextColor(Color.parseColor("#C6A75E"));
            tvOferta.setTextSize(13f);
            tvOferta.setTypeface(null, Typeface.BOLD);
            holder.llBidsContainer.addView(tvOferta);
        }

        // Resultado del lote
        if ("GANADA".equals(estado)) {
            holder.tvResultadoEstado.setText("Ganaste este lote");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#1B5E20"));
        } else if ("PAGADA".equals(estado)) {
            holder.tvResultadoEstado.setText("Ganaste este lote · Pago confirmado");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#1B5E20"));
        } else if ("PERDIDA".equals(estado)) {
            holder.tvResultadoEstado.setText("No ganaste este lote");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#B71C1C"));
        } else if ("GANANDO".equals(estado)) {
            holder.tvResultadoEstado.setText("● Subasta en curso — vas ganando");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#1B5E20"));
        } else if ("PERDIENDO".equals(estado)) {
            holder.tvResultadoEstado.setText("● Subasta en curso — no vas ganando");
            holder.tvResultadoEstado.setTextColor(Color.parseColor("#B71C1C"));
        } else {
            holder.tvResultadoEstado.setText("");
        }

        // Botón "Pagar ahora" solo para GANADA (no pagada aún)
        if ("GANADA".equals(estado)) {
            holder.btnPagarItem.setVisibility(View.VISIBLE);
            holder.btnPagarItem.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, ConfirmarPagoActivity.class);
                intent.putExtra("ITEM_ID", puja.getItemId());
                intent.putExtra("OFERTA", puja.getMiPuja());
                intent.putExtra("COMISION", puja.getComision());
                intent.putExtra("COSTO_ENVIO", puja.getCostoEnvio());
                intent.putExtra("TOTAL_A_PAGAR", puja.getTotalAPagar() > 0
                        ? puja.getTotalAPagar() : puja.getMiPuja());
                intent.putExtra("DESCRIPCION", puja.getProductoDesc());
                context.startActivity(intent);
            });
        } else {
            holder.btnPagarItem.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pujas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductoDesc, tvSubastaDesc, tvResultadoEstado, btnPagarItem;
        LinearLayout llBidsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductoDesc   = itemView.findViewById(R.id.tv_producto_desc);
            tvSubastaDesc    = itemView.findViewById(R.id.tv_subasta_desc);
            llBidsContainer  = itemView.findViewById(R.id.ll_bids_container);
            tvResultadoEstado = itemView.findViewById(R.id.tv_resultado_estado);
            btnPagarItem     = itemView.findViewById(R.id.btn_pagar_item);
        }
    }
}
