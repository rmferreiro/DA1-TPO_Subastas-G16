package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EmpleadoSubastasVivoActivity extends AppCompatActivity {

    private LinearLayout containerItemsVivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_metricas_subastas_vivo);

        containerItemsVivo = findViewById(R.id.container_items_vivo);

        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());
        findViewById(R.id.nav_subastas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoSubastasActivity.class));
        });
        findViewById(R.id.nav_mis_pujas).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoRevisionLotesActivity.class));
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, EmpleadoPanelControlActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderizarItemsEnVivo();
    }

    private void renderizarItemsEnVivo() {
        containerItemsVivo.removeAllViews();
        List<ItemEnVivo> items = MetricasRepository.getInstance().obtenerItemsEnVivo();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (ItemEnVivo item : items) {
            View card = inflater.inflate(R.layout.item_card_en_vivo, containerItemsVivo, false);

            ((TextView) card.findViewById(R.id.txt_nombre)).setText(item.getNombre());
            ((TextView) card.findViewById(R.id.txt_monto)).setText(item.getMontoActual());
            ((TextView) card.findViewById(R.id.txt_cantidad_ofertas))
                    .setText(item.getCantidadOfertas() + " ofertas activas");

            containerItemsVivo.addView(card);
        }
    }
}
