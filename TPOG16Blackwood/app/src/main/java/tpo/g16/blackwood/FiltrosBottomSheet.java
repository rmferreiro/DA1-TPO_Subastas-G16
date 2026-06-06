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

import java.util.Calendar;

public class FiltrosBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestion_filtros_subastas, container, false);

        // ── BOTONES ESTADO ────────────────────────────────────────────
        TextView btnTodas       = view.findViewById(R.id.btn_todas);
        TextView btnProximas    = view.findViewById(R.id.btn_proximas);
        TextView btnEnSala      = view.findViewById(R.id.btn_en_sala);
        TextView btnFinalizadas = view.findViewById(R.id.btn_finalizadas);

        View[] estadoBtns = {btnTodas, btnProximas, btnEnSala, btnFinalizadas};
        seleccionar(btnTodas, estadoBtns);

        for (View btn : estadoBtns) {
            btn.setOnClickListener(v -> seleccionar((TextView) v, estadoBtns));
        }

        // ── BOTONES CATEGORÍA ─────────────────────────────────────────
        TextView btnOro      = view.findViewById(R.id.btn_oro);
        TextView btnPlatino  = view.findViewById(R.id.btn_platino);
        TextView btnDiamante = view.findViewById(R.id.btn_diamante);

        View[] categoriaBtns = {btnOro, btnPlatino, btnDiamante};

        for (View btn : categoriaBtns) {
            btn.setOnClickListener(v -> seleccionar((TextView) v, categoriaBtns));
        }

        // ── SPINNER REMATADOR ─────────────────────────────────────────
        Spinner spinnerRematador = view.findViewById(R.id.spinner_rematador);
        String[] rematadores = {"Todos", "Ruiz", "López", "Gómez", "Pérez"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                rematadores
        );
        spinnerRematador.setAdapter(adapter);

        // ── DATE PICKER FECHA ─────────────────────────────────────────
        EditText inputFecha = view.findViewById(R.id.input_fecha);
        inputFecha.setFocusable(false);
        inputFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                String fecha = String.format("%04d-%02d-%02d", year, month + 1, day);
                inputFecha.setText(fecha);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── APLICAR FILTROS ───────────────────────────────────────────
        view.findViewById(R.id.btn_aplicar_filtros).setOnClickListener(v -> dismiss());

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
}