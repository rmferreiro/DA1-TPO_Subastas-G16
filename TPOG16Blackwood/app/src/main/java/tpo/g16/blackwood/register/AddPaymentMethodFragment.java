package tpo.g16.blackwood.register;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.common.LoadingActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.widget.Toast;
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
    private long editId = -1L;

    // Selecciones de moneda/tipo de identificador
    private String cuentaTipo = "CBU";   // "CBU" o "IBAN"
    private String chequeMoneda = "ARS"; // "ARS" o "USD"

    private EditText etCardNumber;
    private EditText etCardHolder;
    private EditText etCardExpiry;
    private EditText etCardCvv;

    public AddPaymentMethodFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.include_header);
        if (header != null) {
            boolean enHomeActivity = getActivity() instanceof tpo.g16.blackwood.main.HomeActivity;
            if (enHomeActivity) {
                header.setVisibility(View.GONE);
            } else {
                header.setVisibility(View.VISIBLE);
                TextView tvSubtitle = header.findViewById(R.id.header_subtitle);
                if (tvSubtitle != null) tvSubtitle.setText(getString(R.string.agregar_medio_pago));
            }
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

        spinnerTipo.setOnItemClickListener((parent, v, position, id) -> renderFields(position));

        Bundle args = getArguments();
        String initialDetalle = "";
        if (args != null) {
            String editTipo = args.getString("edit_tipo");
            initialDetalle = args.getString("edit_detalle");
            editIndex = args.getInt("edit_index", -1);
            editId = args.getLong("edit_id", -1L);
            String editMonedaExtra = args.getString("moneda_extra", "ARS");
            boolean editEsIntl = args.getBoolean("es_internacional", false);
            cuentaTipo = editEsIntl ? "IBAN" : "CBU";
            chequeMoneda = editMonedaExtra;

            if (editTipo != null) {
                spinnerTipo.setText(editTipo, false);
                int position = Arrays.asList(tipos).indexOf(editTipo);
                if (position != -1) {
                    renderFields(position);
                    if (initialDetalle != null && !initialDetalle.isEmpty()) {
                        String[] parts = initialDetalle.split("; ");
                        // Saltar el primer child si es el selector (LinearLayout)
                        int fieldOffset = 0;
                        if (dynamicFields.getChildCount() > 0
                                && !(dynamicFields.getChildAt(0) instanceof EditText)) {
                            fieldOffset = 1;
                        }
                        int partIdx = 0;
                        for (int i = fieldOffset; i < dynamicFields.getChildCount() && partIdx < parts.length; i++) {
                            View child = dynamicFields.getChildAt(i);
                            if (child instanceof EditText) {
                                ((EditText) child).setText(parts[partIdx++]);
                            }
                        }
                    }
                }
            }
            if (header != null) {
                TextView tvSubtitle = header.findViewById(R.id.header_subtitle);
                if (tvSubtitle != null) tvSubtitle.setText(getString(R.string.editar_medio_pago));
            }
            btnGuardar.setText(getString(R.string.btn_guardar_cambios));
        } else {
            renderFields(0);
            spinnerTipo.setText(tipos[0], false);
        }

        btnGuardar.setOnClickListener(v -> {
            String tipoSeleccionado = spinnerTipo.getText().toString();

            if (tipoSeleccionado.equals(getString(R.string.tipo_tarjeta_credito))) {
                boolean esValido = true;
                String numTarjeta = etCardNumber != null ? etCardNumber.getText().toString().trim() : "";
                String titular = etCardHolder != null ? etCardHolder.getText().toString().trim() : "";
                String vencimiento = etCardExpiry != null ? etCardExpiry.getText().toString().trim() : "";
                String cvv = etCardCvv != null ? etCardCvv.getText().toString().trim() : "";

                if (etCardNumber != null) etCardNumber.setBackgroundResource(R.drawable.input_bg);
                if (etCardHolder != null) etCardHolder.setBackgroundResource(R.drawable.input_bg);
                if (etCardExpiry != null) etCardExpiry.setBackgroundResource(R.drawable.input_bg);
                if (etCardCvv != null) etCardCvv.setBackgroundResource(R.drawable.input_bg);

                if (numTarjeta.length() != 16) {
                    if (etCardNumber != null) etCardNumber.setBackgroundResource(R.drawable.input_bg_error);
                    Toast.makeText(getContext(), getString(R.string.error_tarjeta_dieciseis_digitos), Toast.LENGTH_SHORT).show();
                    esValido = false;
                } else if (titular.isEmpty()) {
                    if (etCardHolder != null) etCardHolder.setBackgroundResource(R.drawable.input_bg_error);
                    Toast.makeText(getContext(), "El nombre del titular es obligatorio", Toast.LENGTH_SHORT).show();
                    esValido = false;
                } else if (vencimiento.length() != 5) {
                    if (etCardExpiry != null) etCardExpiry.setBackgroundResource(R.drawable.input_bg_error);
                    Toast.makeText(getContext(), getString(R.string.error_vencimiento_formato), Toast.LENGTH_SHORT).show();
                    esValido = false;
                } else if (cvv.length() != 3) {
                    if (etCardCvv != null) etCardCvv.setBackgroundResource(R.drawable.input_bg_error);
                    Toast.makeText(getContext(), getString(R.string.error_cvv_tres_digitos), Toast.LENGTH_SHORT).show();
                    esValido = false;
                }

                if (!esValido) return;
            }

            // Recolectar solo los EditText del dynamicFields
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dynamicFields.getChildCount(); i++) {
                View fieldView = dynamicFields.getChildAt(i);
                if (fieldView instanceof EditText) {
                    String val = ((EditText) fieldView).getText().toString().trim();
                    if (sb.length() > 0) sb.append("; ");
                    sb.append(val);
                }
            }

            String detalle = sb.toString();
            if (detalle.trim().isEmpty() || detalle.replace(";", "").trim().isEmpty()) {
                detalle = getString(R.string.detalle_vacio);
            }

            Bundle result = new Bundle();
            result.putString("tipo", tipoSeleccionado);
            result.putString("detalle", detalle);
            result.putInt("edit_index", editIndex);
            result.putLong("edit_id", editId);

            // Datos de moneda e internacional para MediosPagoFragment
            if (tipoSeleccionado.equals(getString(R.string.tipo_cuenta_bancaria))) {
                result.putString("moneda_extra", "CBU".equals(cuentaTipo) ? "ARS" : "USD");
                result.putBoolean("es_internacional", "IBAN".equals(cuentaTipo));
            } else if (tipoSeleccionado.equals(getString(R.string.tipo_cheque_certificado))) {
                result.putString("moneda_extra", chequeMoneda);
                result.putBoolean("es_internacional", false);
            } else {
                result.putString("moneda_extra", "ARS");
                result.putBoolean("es_internacional", false);
            }

            getParentFragmentManager().setFragmentResult("add_payment_request", result);

            if (editId <= 0 && editIndex == -1) {
                Intent loadingIntent = new Intent(getActivity(), LoadingActivity.class);
                loadingIntent.putExtra(LoadingActivity.EXTRA_TITLE, getString(R.string.loading_verificando_medio));
                loadingIntent.putExtra(LoadingActivity.EXTRA_DESC, getString(R.string.loading_vinculando_medio, tipoSeleccionado));
                loadingIntent.putExtra(LoadingActivity.EXTRA_INFO, getString(R.string.loading_info_pujas));
                loadingIntent.putExtra(LoadingActivity.EXTRA_DURATION, 2000);
                startActivity(loadingIntent);
            }

            getParentFragmentManager().popBackStack();
        });
    }

    private void renderFields(int type) {
        dynamicFields.removeAllViews();

        etCardNumber = null;
        etCardHolder = null;
        etCardExpiry = null;
        etCardCvv = null;

        if (type == 0) {
            // Tarjeta de crédito
            etCardNumber = createInput(getString(R.string.input_num_tarjeta), InputType.TYPE_CLASS_NUMBER);
            etCardHolder = createInput(getString(R.string.titular_ph), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            etCardExpiry = createInput(getString(R.string.input_vencimiento), InputType.TYPE_CLASS_DATETIME);
            etCardCvv = createInput(getString(R.string.input_cvv), InputType.TYPE_CLASS_NUMBER);

            etCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
            etCardNumber.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            etCardExpiry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
            etCardExpiry.setKeyListener(DigitsKeyListener.getInstance("0123456789/"));
            etCardCvv.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            etCardCvv.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

            etCardExpiry.addTextChangedListener(new TextWatcher() {
                private String current = "";
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().equals(current)) {
                        String clean = s.toString().replaceAll("[^\\d]", "");
                        String formatted = clean;
                        if (clean.length() > 2) formatted = clean.substring(0, 2) + "/" + clean.substring(2);
                        else if (clean.length() == 2 && before < count) formatted = clean + "/";
                        if (formatted.length() > 5) formatted = formatted.substring(0, 5);
                        current = formatted;
                        etCardExpiry.removeTextChangedListener(this);
                        etCardExpiry.setText(formatted);
                        etCardExpiry.setSelection(formatted.length());
                        etCardExpiry.addTextChangedListener(this);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            addClearErrorWatcher(etCardNumber);
            addClearErrorWatcher(etCardHolder);
            addClearErrorWatcher(etCardExpiry);
            addClearErrorWatcher(etCardCvv);

            dynamicFields.addView(etCardNumber);
            dynamicFields.addView(etCardHolder);
            dynamicFields.addView(etCardExpiry);
            dynamicFields.addView(etCardCvv);

        } else if (type == 1) {
            // Cuenta bancaria con selector CBU/IBAN
            dynamicFields.addView(crearSelectorToggle("CBU", "IBAN", cuentaTipo, selected -> {
                cuentaTipo = selected;
                // Actualizar hint del campo de número
                for (int i = 0; i < dynamicFields.getChildCount(); i++) {
                    View child = dynamicFields.getChildAt(i);
                    if (child instanceof EditText && i == 2) {
                        ((EditText) child).setHint("CBU".equals(selected) ? "CBU (22 dígitos)" : "IBAN");
                    }
                }
            }));
            dynamicFields.addView(createInput(getString(R.string.banco_ph), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
            dynamicFields.addView(createInput(getString(R.string.input_num_cuenta), InputType.TYPE_CLASS_NUMBER));
            dynamicFields.addView(createInput("CBU".equals(cuentaTipo) ? "CBU (22 dígitos)" : "IBAN", InputType.TYPE_CLASS_TEXT));

        } else {
            // Cheque certificado con selector ARS/USD
            dynamicFields.addView(crearSelectorToggle("ARS", "USD", chequeMoneda, selected -> chequeMoneda = selected));
            dynamicFields.addView(createInput(getString(R.string.input_banco_emisor), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
            dynamicFields.addView(createInput(getString(R.string.input_num_cheque), InputType.TYPE_CLASS_NUMBER));
        }
    }

    interface OnToggleSelected {
        void onSelected(String value);
    }

    private LinearLayout crearSelectorToggle(String opcion1, String opcion2, String seleccionActual, OnToggleSelected listener) {
        LinearLayout container = new LinearLayout(requireContext());
        int heightPx = dpToPx(44);
        int marginPx = dpToPx(14);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
        containerParams.bottomMargin = marginPx;
        container.setLayoutParams(containerParams);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackground(createSelectorBackground());

        TextView btn1 = crearBotonToggle(opcion1, seleccionActual.equals(opcion1));
        TextView btn2 = crearBotonToggle(opcion2, seleccionActual.equals(opcion2));

        btn1.setOnClickListener(v -> {
            actualizarEstadoBoton(btn1, true);
            actualizarEstadoBoton(btn2, false);
            listener.onSelected(opcion1);
        });
        btn2.setOnClickListener(v -> {
            actualizarEstadoBoton(btn1, false);
            actualizarEstadoBoton(btn2, true);
            listener.onSelected(opcion2);
        });

        container.addView(btn1);
        container.addView(btn2);
        return container;
    }

    private TextView crearBotonToggle(String texto, boolean activo) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        tv.setLayoutParams(params);
        tv.setText(texto);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        actualizarEstadoBoton(tv, activo);
        return tv;
    }

    private void actualizarEstadoBoton(TextView tv, boolean activo) {
        tv.setBackgroundColor(activo ? Color.parseColor("#1C2A21") : Color.TRANSPARENT);
        tv.setTextColor(activo ? Color.parseColor("#C6A75E") : Color.parseColor("#6B6B6B"));
    }

    private android.graphics.drawable.GradientDrawable createSelectorBackground() {
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(8));
        bg.setStroke(dpToPx(1), Color.parseColor("#1C2A21"));
        bg.setColor(Color.parseColor("#F4F1EA"));
        return bg;
    }

    private EditText createInput(String hint, int inputType) {
        EditText editText = new EditText(requireContext());
        int heightPx = dpToPx(55);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
        params.bottomMargin = dpToPx(14);
        editText.setLayoutParams(params);
        editText.setHint(hint);
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.charcoal));
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.gray_medium));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_body));
        int padding = dpToPx(16);
        editText.setPadding(padding, 0, padding, 0);
        editText.setGravity(android.view.Gravity.CENTER_VERTICAL);
        editText.setBackgroundResource(R.drawable.input_bg);
        editText.setInputType(inputType);
        return editText;
    }

    private void addClearErrorWatcher(EditText et) {
        if (et == null) return;
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { et.setBackgroundResource(R.drawable.input_bg); }
        });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
