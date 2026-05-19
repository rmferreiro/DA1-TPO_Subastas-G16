package tpo.g16.blackwood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroPaso2Activity extends AppCompatActivity {

    private Button btnAgregarMetodo;
    private ScrollView mainContent;
    private FrameLayout fragmentContainer;
    private LinearLayout containerMetodosPago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_paso2);

        // Referencias
        btnAgregarMetodo = findViewById(R.id.btn_agregar_metodo);
        mainContent = findViewById(R.id.main_registration_content);
        fragmentContainer = findViewById(R.id.fragment_container);
        containerMetodosPago = findViewById(R.id.container_metodos_pago);

        // Listener para abrir el fragmento
        btnAgregarMetodo.setOnClickListener(v -> {
            mainContent.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);

            AddPaymentMethodFragment fragment = new AddPaymentMethodFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Listener para recibir los datos del fragmento cuando se guarda
        getSupportFragmentManager().setFragmentResultListener("add_payment_request", this, (requestKey, bundle) -> {
            String tipo = bundle.getString("tipo");
            String detalle = bundle.getString("detalle");
            agregarNuevaTarjeta(tipo, detalle);
        });

        // Controlar visibilidad al volver atrás
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                mainContent.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
            }
        });
    }

    private void agregarNuevaTarjeta(String tipo, String detalle) {
        // Inflar el diseño de la card
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_metodo_pago, containerMetodosPago, false);

        // Configurar los textos
        TextView tvTitulo = cardView.findViewById(R.id.tv_metodo_titulo);
        TextView tvDetalle = cardView.findViewById(R.id.tv_metodo_detalle);

        tvTitulo.setText(tipo);
        tvDetalle.setText(detalle);

        // Agregar a la lista
        containerMetodosPago.addView(cardView);
    }
}
