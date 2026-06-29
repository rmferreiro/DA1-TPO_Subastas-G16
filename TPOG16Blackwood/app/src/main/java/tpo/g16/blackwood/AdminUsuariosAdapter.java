package tpo.g16.blackwood;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminUsuariosAdapter extends RecyclerView.Adapter<AdminUsuariosAdapter.ViewHolder> {

    // Colores para el estado seleccionado / no seleccionado de los botones de categoría
    private static final int COLOR_SELECCIONADO_FONDO = Color.parseColor("#C6A75E");
    private static final int COLOR_SELECCIONADO_TEXTO = Color.WHITE;
    private static final int COLOR_NORMAL_FONDO       = Color.TRANSPARENT;
    private static final int COLOR_NORMAL_TEXTO        = Color.parseColor("#C6A75E");

    private List<Map<String, Object>> items = new ArrayList<>();
    private final OnAprobarClickListener listener;

    public interface OnAprobarClickListener {
        void onAprobarClick(String email, String categoria, int position);
    }

    public AdminUsuariosAdapter(OnAprobarClickListener listener) {
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
                .inflate(R.layout.item_admin_usuario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);

        String nombre = buildNombreCompleto(item);
        String email = getStr(item, "email");
        String documento = getStr(item, "documento");
        String pais = getStr(item, "pais");

        holder.tvNombre.setText(nombre);
        holder.tvEmail.setText(email != null ? email : "Sin correo");
        holder.tvDocumento.setText(documento != null ? documento : "—");
        holder.tvPais.setText(pais != null ? pais : "—");

        // Categoría seleccionada por defecto: bronce
        final String[] categoriaSeleccionada = {"bronce"};
        seleccionarCategoria(holder, "bronce");

        holder.btnBronce.setOnClickListener(v -> {
            categoriaSeleccionada[0] = "bronce";
            seleccionarCategoria(holder, "bronce");
        });
        holder.btnPlata.setOnClickListener(v -> {
            categoriaSeleccionada[0] = "plata";
            seleccionarCategoria(holder, "plata");
        });
        holder.btnOro.setOnClickListener(v -> {
            categoriaSeleccionada[0] = "oro";
            seleccionarCategoria(holder, "oro");
        });

        holder.btnAprobar.setEnabled(true);
        holder.btnAprobar.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (email == null || email.isEmpty()) return;
            holder.btnAprobar.setEnabled(false);
            listener.onAprobarClick(email, categoriaSeleccionada[0], pos);
        });
    }

    private void seleccionarCategoria(ViewHolder holder, String categoria) {
        // Resetear los tres botones
        resetBotonCategoria(holder.btnBronce);
        resetBotonCategoria(holder.btnPlata);
        resetBotonCategoria(holder.btnOro);

        // Resaltar el seleccionado
        switch (categoria) {
            case "bronce": activarBotonCategoria(holder.btnBronce); break;
            case "plata":  activarBotonCategoria(holder.btnPlata);  break;
            case "oro":    activarBotonCategoria(holder.btnOro);    break;
        }
    }

    private void resetBotonCategoria(MaterialButton btn) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_NORMAL_FONDO));
        btn.setTextColor(COLOR_NORMAL_TEXTO);
    }

    private void activarBotonCategoria(MaterialButton btn) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_SELECCIONADO_FONDO));
        btn.setTextColor(COLOR_SELECCIONADO_TEXTO);
    }

    private String buildNombreCompleto(Map<String, Object> item) {
        String nombre = getStr(item, "nombre");
        String apellido = getStr(item, "apellido");
        if (nombre != null && apellido != null) return nombre + " " + apellido;
        if (nombre != null) return nombre;
        return "Usuario desconocido";
    }

    private String getStr(Map<String, Object> item, String key) {
        Object val = item.get(key);
        return val instanceof String ? (String) val : null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvEmail;
        TextView tvDocumento;
        TextView tvPais;
        MaterialButton btnBronce;
        MaterialButton btnPlata;
        MaterialButton btnOro;
        Button btnAprobar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre    = itemView.findViewById(R.id.tv_usuario_nombre);
            tvEmail     = itemView.findViewById(R.id.tv_usuario_email);
            tvDocumento = itemView.findViewById(R.id.tv_usuario_documento);
            tvPais      = itemView.findViewById(R.id.tv_usuario_pais);
            btnBronce   = itemView.findViewById(R.id.btn_cat_bronce);
            btnPlata    = itemView.findViewById(R.id.btn_cat_plata);
            btnOro      = itemView.findViewById(R.id.btn_cat_oro);
            btnAprobar  = itemView.findViewById(R.id.btn_aprobar_usuario);
        }
    }
}
