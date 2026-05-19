package tpo.g16.blackwood;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class AddPaymentMethodFragment extends Fragment {

    private Spinner spinnerTipo;
    private LinearLayout dynamicFields;
    private Button btnGuardar;

    public AddPaymentMethodFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_add_payment_method, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        spinnerTipo = view.findViewById(R.id.spinner_tipo_pago);
        dynamicFields = view.findViewById(R.id.dynamic_fields);
        btnGuardar = view.findViewById(R.id.btn_guardar_pago);

        String[] tipos = {
                "Tarjeta de crédito",
                "Cuenta bancaria",
                "Cheque certificado"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                tipos
        );

        spinnerTipo.setAdapter(adapter);

        spinnerTipo.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        renderFields(position);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {

                    }
                }
        );

        btnGuardar.setOnClickListener(v -> {
            String tipoSeleccionado = spinnerTipo.getSelectedItem().toString();
            // Para este ejemplo, tomaremos el primer campo como detalle representativo
            String detalle = "Nuevo método agregado";
            if (dynamicFields.getChildCount() > 0 && dynamicFields.getChildAt(0) instanceof EditText) {
                String val = ((EditText) dynamicFields.getChildAt(0)).getText().toString();
                if (!val.isEmpty()) {
                    detalle = val;
                }
            }

            Bundle result = new Bundle();
            result.putString("tipo", tipoSeleccionado);
            result.putString("detalle", detalle);
            getParentFragmentManager().setFragmentResult("add_payment_request", result);
            
            // Volver atrás
            getParentFragmentManager().popBackStack();
        });

        renderFields(0);
    }

    private void renderFields(int type) {
        dynamicFields.removeAllViews();
        if (type == 0) {
            dynamicFields.addView(createInput("Número de tarjeta"));
            dynamicFields.addView(createInput("Titular"));
            dynamicFields.addView(createInput("Vencimiento MM/AA"));
            dynamicFields.addView(createInput("CVV"));
        } else if (type == 1) {
            dynamicFields.addView(createInput("Banco"));
            dynamicFields.addView(createInput("Número de cuenta"));
            dynamicFields.addView(createInput("CBU / IBAN"));
            dynamicFields.addView(createInput("País"));
        } else {
            dynamicFields.addView(createInput("Banco emisor"));
            dynamicFields.addView(createInput("Monto disponible"));
            dynamicFields.addView(createInput("Número de cheque"));
        }
    }

    private EditText createInput(String hint) {
        EditText editText = new EditText(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = 24;
        editText.setLayoutParams(params);
        editText.setHint(hint);
        editText.setTextColor(0xFF1A1A1A);
        editText.setHintTextColor(0xFF6B6B6B);
        editText.setPadding(32, 28, 32, 28);
        editText.setBackgroundResource(R.drawable.input_bg);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        return editText;
    }
}
