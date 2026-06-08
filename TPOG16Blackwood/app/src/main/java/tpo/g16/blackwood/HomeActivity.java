package tpo.g16.blackwood;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout tabSubastas, tabMisPujas, tabPerfil;
    private TextView labelSubastas, labelMisPujas, labelPerfil;
    private View dotSubastas, dotMisPujas, dotPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tabSubastas = findViewById(R.id.tab_subastas);
        tabMisPujas = findViewById(R.id.tab_mis_pujas);
        tabPerfil   = findViewById(R.id.tab_perfil);

        labelSubastas = findViewById(R.id.tab_subastas_label);
        labelMisPujas = findViewById(R.id.tab_pujas_label);
        labelPerfil   = findViewById(R.id.tab_perfil_label);

        dotSubastas = findViewById(R.id.tab_subastas_dot);
        dotMisPujas = findViewById(R.id.tab_pujas_dot);
        dotPerfil   = findViewById(R.id.tab_perfil_dot);

        tabSubastas.setOnClickListener(v -> selectTab(0));
        tabMisPujas.setOnClickListener(v -> selectTab(1));
        tabPerfil.setOnClickListener(v -> selectTab(2));

        // Por defecto:
        int defaultTab = getIntent().getIntExtra("TAB_INDEX", 0);
        selectTab(defaultTab);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int defaultTab = intent.getIntExtra("TAB_INDEX", 0);
        selectTab(defaultTab);
    }

    public void selectTab(int index) {
        // Reset
        labelSubastas.setTextColor(Color.parseColor("#6B6B6B"));
        labelMisPujas.setTextColor(Color.parseColor("#6B6B6B"));
        labelPerfil.setTextColor(Color.parseColor("#6B6B6B"));

        dotSubastas.setVisibility(View.INVISIBLE);
        dotMisPujas.setVisibility(View.INVISIBLE);
        dotPerfil.setVisibility(View.INVISIBLE);

        Fragment selectedFragment = null;

        switch (index) {
            case 0: // Subastas
                labelSubastas.setTextColor(Color.parseColor("#1C2A21"));
                dotSubastas.setVisibility(View.VISIBLE);
                selectedFragment = new ListaSubastasFragment();
                break;
            case 1: // Mis Pujas
                labelMisPujas.setTextColor(Color.parseColor("#1C2A21"));
                dotMisPujas.setVisibility(View.VISIBLE);
                selectedFragment = new MisPujasFragment();
                break;
            case 2: // Perfil
                labelPerfil.setTextColor(Color.parseColor("#1C2A21"));
                dotPerfil.setVisibility(View.VISIBLE);
                selectedFragment = new PerfilFragment();
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }
    }
}
