package tpo.g16.blackwood;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.util.Calendar;

public class FiltrosBottomSheet extends BottomSheetDialogFragment {

    /** Quien abre este bottom sheet implementa esto para recibir la selección. */
    public interface OnFiltrosAplicadosListener {
        /**
         * @param estado    "Todas", "Próximas", "En sala" o "Finalizadas"
         * @param categoria nombre de la categoría elegida, o null si no se eligió ninguna
         * @param rematador nombre del rematador elegido, o null si es "Todos"
         * @param fecha     fecha elegida, o null si no se elige ninguna
         */
        void onFiltrosAplicados(String estado, String categoria, String rematador, LocalDate fecha);
    }

    private OnFiltrosAplicadosListener listener;

    public void setOnFiltrosAplicadosListener(OnFiltrosAplicadosListener listener) {
        this.listener = listener;
    }

    private View[] estadoBtns;
    private View[] categoriaBtns;
    private Spinner spinnerRematador;
    private LocalDate fechaSeleccionada;

    // Filtros con los que se abre el sheet (para recordar la última selección)
    private String estadoInicial = "Todas";
    private String categoriaInicial = null;
    private String rematadorInicial = null;
    private LocalDate fechaInicial = null;

    /** Llamar ANTES de show() para que el sheet abra con los filtros que ya estaban aplicados. */
    public void setFiltrosActuales(String estado, String categoria, String rematador, LocalDate fecha) {
        this.estadoInicial = (estado != null) ? estado : "Todas";
        this.categoriaInicial = categoria;
        this.rematadorInicial = rematador;
        this.fechaInicial = fecha;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestion_filtros_subastas, container, false);

        // ── BOTONES ESTADO ────────────────────────────────────────────
        TextView btnTodas       = view.findViewById(R.id.btn_todas);
        TextView btnProximas    = view.findViewById(R.id.btn_proximas);
        TextView btnEnSala      = view.findViewById(R.id.btn_en_sala);
        TextView btnFinalizadas = view.findViewById(R.id.btn_finalizadas);

        estadoBtns = new View[]{btnTodas, btnProximas, btnEnSala, btnFinalizadas};
        seleccionar((TextView) buscarPorTexto(estadoBtns, estadoInicial, btnTodas), estadoBtns);

        for (View btn : estadoBtns) {
            btn.setOnClickListener(v -> seleccionar((TextView) v, estadoBtns));
        }

        // ── BOTONES CATEGORÍA ─────────────────────────────────────────
        TextView btnComun    = view.findViewById(R.id.btn_comun);
        TextView btnEspecial = view.findViewById(R.id.btn_especial);
        TextView btnPlata    = view.findViewById(R.id.btn_plata);
        TextView btnOro      = view.findViewById(R.id.btn_oro);
        TextView btnPlatino  = view.findViewById(R.id.btn_platino);

        categoriaBtns = new View[]{btnComun, btnEspecial, btnPlata, btnOro, btnPlatino};

        if (categoriaInicial != null) {
            View seleccionada = buscarPorTexto(categoriaBtns, categoriaInicial, null);
            if (seleccionada != null) seleccionar((TextView) seleccionada, categoriaBtns);
        }

        // Toggle: si tocás el chip que ya estaba seleccionado, se deselecciona
        // (vuelve a "todas las categorías"). Si tocás otro, pasa a ser el único elegido.
        for (View btn : categoriaBtns) {
            btn.setOnClickListener(v -> {
                boolean yaEstabaSeleccionado = ((TextView) v).getCurrentTextColor() == 0xFFF4F1EA;
                if (yaEstabaSeleccionado) {
                    deseleccionarTodos(categoriaBtns);
                } else {
                    seleccionar((TextView) v, categoriaBtns);
                }
            });
        }

        // ── SPINNER REMATADOR ─────────────────────────────────────────
        spinnerRematador = view.findViewById(R.id.spinner_rematador);
        String[] rematadores = {"Todos", "Ruiz", "López", "Gómez", "Pérez"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                rematadores
        );
        spinnerRematador.setAdapter(adapter);
        if (rematadorInicial != null) {
            int index = java.util.Arrays.asList(rematadores).indexOf(rematadorInicial);
            if (index >= 0) spinnerRematador.setSelection(index);
        }

        // ── DATE PICKER FECHA ─────────────────────────────────────────
        EditText inputFecha = view.findViewById(R.id.input_fecha);
        inputFecha.setFocusable(false);
        fechaSeleccionada = fechaInicial;
        if (fechaInicial != null) {
            inputFecha.setText(FechaUtils.formatear(fechaInicial));
        }
        inputFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                fechaSeleccionada = LocalDate.of(year, month + 1, day);
                inputFecha.setText(FechaUtils.formatear(fechaSeleccionada));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── QUITAR TODOS LOS FILTROS ──────────────────────────────────
        view.findViewById(R.id.btn_quitar_filtros).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFiltrosAplicados("Todas", null, null, null);
            }
            dismiss();
        });

        // ── APLICAR FILTROS ───────────────────────────────────────────
        view.findViewById(R.id.btn_aplicar_filtros).setOnClickListener(v -> {
            if (listener != null) {
                String estado = obtenerSeleccionado(estadoBtns);
                String categoria = obtenerSeleccionado(categoriaBtns);
                String rematador = (String) spinnerRematador.getSelectedItem();
                if ("Todos".equals(rematador)) rematador = null;
                listener.onFiltrosAplicados(estado, categoria, rematador, fechaSeleccionada);
            }
            dismiss();
        });

        return view;
    }

    private void seleccionar(TextView seleccionado, View[] grupo) {
        for (View btn : grupo) {
            btn.setBackgroundResource(R.drawable.input_bg);
            ((TextView) btn).setTextColor(0xFF1A1A1A);
        }
        seleccionado.setBackgroundResource(R.drawable.button_primary);
        seleccionado.setTextColor(0xFFF4F1EA);
    }

    /** Saca la selección de todo el grupo (queda como "todas las opciones", sin filtrar por esto). */
    private void deseleccionarTodos(View[] grupo) {
        for (View btn : grupo) {
            btn.setBackgroundResource(R.drawable.input_bg);
            ((TextView) btn).setTextColor(0xFF1A1A1A);
        }
    }

    /** Busca dentro del grupo el botón cuyo texto coincide (sin importar mayúsculas), o el de respaldo si no encuentra. */
    private View buscarPorTexto(View[] grupo, String texto, View porDefecto) {
        if (texto != null) {
            for (View btn : grupo) {
                if (((TextView) btn).getText().toString().equalsIgnoreCase(texto)) {
                    return btn;
                }
            }
        }
        return porDefecto;
    }

    /** Devuelve el texto del botón seleccionado dentro del grupo, o null si ninguno lo está. */
    private String obtenerSeleccionado(View[] grupo) {
        for (View btn : grupo) {
            if (((TextView) btn).getCurrentTextColor() == 0xFFF4F1EA) {
                return ((TextView) btn).getText().toString();
            }
        }
        return null;
    }
}
