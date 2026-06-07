package tpo.g16.blackwood.register;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.common.LoadingActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

public class AddPaymentMethodFragment extends Fragment {

    private AutoCompleteTextView spinnerTipo;
    private LinearLayout dynamicFields;
    private Button btnGuardar;
    private int editIndex = -1;

    public AddPaymentMethodFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar Header
        View header = view.findViewById(R.id.include_header);
        if (header != null) {
            TextView tvSubtitle = header.findViewById(R.id.header_subtitle);
            tvSubtitle.setText(getString(R.string.agregar_medio_pago));
        }

        spinnerTipo = view.findViewById(R.id.spinner_tipo_pago);
        dynamicFields = view.findViewById(R.id.dynamic_fields);
        btnGuardar = view.findViewById(R.id.btn_guardar_pago);

        String[] tipos = {
                getString(R.string.tipo_tarjeta_credito),
                getString(R.string.tipo_cuenta_bancaria),
                getString(R.string.tipo_cheque_certificado)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, tipos);
        spinnerTipo.setAdapter(adapter);

        spinnerTipo.setOnItemClickListener((parent, v, position, id) -> {
            renderFields(position);
        });

        // Revisar si estamos editando
        Bundle args = getArguments();
        String initialDetalle = "";
        if (args != null) {
            String editTipo = args.getString("edit_tipo");
            initialDetalle = args.getString("edit_detalle");
            editIndex = args.getInt("edit_index", -1);

            if (editTipo != null) {
                spinnerTipo.setText(editTipo, false);
                int position = Arrays.asList(tipos).indexOf(editTipo);
                if (position != -1) {
                    renderFields(position);
                    
                    // Lógica de Persistencia: Repartir el detalle en los campos
                    if (initialDetalle != null && !initialDetalle.isEmpty()) {
                        String[] parts = initialDetalle.split("; ");
                        for (int i = 0; i < parts.length && i < dynamicFields.getChildCount(); i++) {
                            View child = dynamicFields.getChildAt(i);
                            if (child instanceof EditText) {
                                ((EditText) child).setText(parts[i]);
                            }
                        }
                    }
                }
            }
            if (header != null) {
                TextView tvSubtitle = header.findViewById(R.id.header_subtitle);
                tvSubtitle.setText(getString(R.string.editar_medio_pago));
            }
            btnGuardar.setText(getString(R.string.btn_guardar_cambios));
        } else {
            renderFields(0);
            spinnerTipo.setText(tipos[0], false);
        }

        btnGuardar.setOnClickListener(v -> {
            String tipoSeleccionado = spinnerTipo.getText().toString();
            
            // Recolectamos TODOS los campos dinámicos para persistencia completa
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dynamicFields.getChildCount(); i++) {
                View fieldView = dynamicFields.getChildAt(i);
                if (fieldView instanceof EditText) {
                    String val = ((EditText) fieldView).getText().toString().trim();
                    sb.append(val);
                    if (i < dynamicFields.getChildCount() - 1) sb.append("; ");
                }
            }
            
            String detalle = sb.toString();
            if (detalle.trim().isEmpty() || detalle.replace(";", "").trim().isEmpty()) {
                detalle = getString(R.string.detalle_vacio);
            }

            // 1. Preparar el resultado para la actividad principal
            Bundle result = new Bundle();
            result.putString("tipo", tipoSeleccionado);
            result.putString("detalle", detalle);
            result.putInt("edit_index", editIndex);
            getParentFragmentManager().setFragmentResult("add_payment_request", result);

            // 2. Lanzar pantalla de procesamiento (solo si es nuevo)
            if (editIndex == -1) {
                Intent loadingIntent = new Intent(getActivity(), LoadingActivity.class);
                loadingIntent.putExtra(LoadingActivity.EXTRA_TITLE, getString(R.string.loading_verificando_medio));
                loadingIntent.putExtra(LoadingActivity.EXTRA_DESC, getString(R.string.loading_vinculando_medio, tipoSeleccionado));
                loadingIntent.putExtra(LoadingActivity.EXTRA_INFO, getString(R.string.loading_info_pujas));
                loadingIntent.putExtra(LoadingActivity.EXTRA_DURATION, 2000);
                startActivity(loadingIntent);
            }

            // 3. Volver a la pantalla anterior
            getParentFragmentManager().popBackStack();
        });
    }

    private void renderFields(int type) {
        dynamicFields.removeAllViews();
        if (type == 0) {
            dynamicFields.addView(createInput(getString(R.string.input_num_tarjeta), InputType.TYPE_CLASS_NUMBER));
            dynamicFields.addView(createInput(getString(R.string.titular_ph), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
            dynamicFields.addView(createInput(getString(R.string.input_vencimiento), InputType.TYPE_CLASS_DATETIME));
            dynamicFields.addView(createInput(getString(R.string.input_cvv), InputType.TYPE_CLASS_NUMBER));
        } else if (type == 1) {
            dynamicFields.addView(createInput(getString(R.string.banco_ph), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
            dynamicFields.addView(createInput(getString(R.string.input_num_cuenta), InputType.TYPE_CLASS_NUMBER));
            dynamicFields.addView(createInput(getString(R.string.input_cbu), InputType.TYPE_CLASS_NUMBER));
        } else {
            dynamicFields.addView(createInput(getString(R.string.input_banco_emisor), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
            dynamicFields.addView(createInput(getString(R.string.input_num_cheque), InputType.TYPE_CLASS_NUMBER));
        }
    }

    private EditText createInput(String hint, int inputType) {
        EditText editText = new EditText(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        editText.setLayoutParams(params);
        editText.setHint(hint);
        
        // Usando colores de la nueva paleta: Marfil (#F4F1EA) de fondo está en el layout, texto oscuro
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.charcoal));      
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.gray_medium));  

        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_body));

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        editText.setPadding(padding, 0, padding, 0);
        editText.setHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, getResources().getDisplayMetrics()));
        
        editText.setBackgroundResource(R.drawable.input_bg);
        editText.setInputType(inputType);
        return editText;
    }
}
