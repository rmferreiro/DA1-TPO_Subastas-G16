package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoDetalleSubastaActivity extends AppCompatActivity {

    // Estados posibles: NO_INICIADA, EN_PROCESO, TERMINADA
    public static final String ESTADO = "estado";
    public static final int NO_INICIADA = 0;
    public static final int EN_PROCESO = 1;
    public static final int TERMINADA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_detalle_subasta);

        int estado = getIntent().getIntExtra(ESTADO, NO_INICIADA);

        Button btnPrincipal = findViewById(R.id.btn_accion_principal);
        Button btnFinalizar = findViewById(R.id.btn_finalizar);

        switch (estado) {
            case NO_INICIADA:
                // Verde: Empezar Subasta + gris: Finalizar Subasta
                btnPrincipal.setText("Empezar Subasta");
                btnPrincipal.setBackgroundResource(R.drawable.button_primary);
                btnPrincipal.setBackgroundTintList(null);
                btnFinalizar.setVisibility(android.view.View.VISIBLE);
                btnFinalizar.setText("Finalizar Subasta");
                break;

            case EN_PROCESO:
                // Dorado: Unirse a la puja + rojo: Finalizar Subasta
                btnPrincipal.setText("Unirse a la puja");
                btnPrincipal.setBackgroundResource(R.drawable.button_gold);
                btnPrincipal.setBackgroundTintList(null);
                btnFinalizar.setVisibility(android.view.View.VISIBLE);
                btnFinalizar.setText("Finalizar Subasta");
                btnFinalizar.setBackgroundResource(R.drawable.button_danger);
                btnFinalizar.setBackgroundTintList(null);
                break;

            case TERMINADA:
                // Sin botones
                btnPrincipal.setVisibility(android.view.View.GONE);
                btnFinalizar.setVisibility(android.view.View.GONE);
                break;
        }

        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());

        btnPrincipal.setOnClickListener(v -> {
            if (estado == NO_INICIADA) {
                // Reabrir con estado EN_PROCESO
                android.content.Intent intent = new android.content.Intent(this, EmpleadoDetalleSubastaActivity.class);
                intent.putExtra(ESTADO, EN_PROCESO);
                startActivity(intent);
                finish();
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            // Volver a lista de subastas
            android.content.Intent intent = new android.content.Intent(this, EmpleadoSubastasActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });


        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });

    }
}
