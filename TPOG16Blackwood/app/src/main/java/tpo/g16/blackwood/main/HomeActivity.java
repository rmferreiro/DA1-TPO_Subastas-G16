package tpo.g16.blackwood.main;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.subastas.ListaSubastasFragment;
import tpo.g16.blackwood.subastas.MisPujasFragment;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout tabSubastas, tabMisPujas, tabPerfil;
    private TextView labelSubastas, labelMisPujas, labelPerfil;
    private View dotSubastas, dotMisPujas, dotPerfil;
    private TextView homeHeaderSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Habilitar edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

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

        homeHeaderSubtitle = findViewById(R.id.home_header_subtitle);

        tabSubastas.setOnClickListener(v -> selectTab(0));
        tabMisPujas.setOnClickListener(v -> selectTab(1));
        tabPerfil.setOnClickListener(v -> selectTab(2));

        // Aplicar padding dinámico por la barra de estado y de navegación
        View rootView = findViewById(R.id.home_root);
        View headerView = findViewById(R.id.home_header);
        View bottomNav = findViewById(R.id.bottom_nav_include);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            headerView.setPadding(headerView.getPaddingLeft(), systemBars.top, headerView.getPaddingRight(), headerView.getPaddingBottom());
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

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

        labelSubastas.setTypeface(null, Typeface.NORMAL);
        labelMisPujas.setTypeface(null, Typeface.NORMAL);
        labelPerfil.setTypeface(null, Typeface.NORMAL);

        dotSubastas.setVisibility(View.INVISIBLE);
        dotMisPujas.setVisibility(View.INVISIBLE);
        dotPerfil.setVisibility(View.INVISIBLE);

        Fragment selectedFragment = null;

        switch (index) {
            case 0: // Subastas
                labelSubastas.setTextColor(Color.parseColor("#1C2A21"));
                labelSubastas.setTypeface(null, Typeface.BOLD);
                dotSubastas.setVisibility(View.VISIBLE);
                if (homeHeaderSubtitle != null) {
                    homeHeaderSubtitle.setText("Subastas disponibles");
                }
                selectedFragment = new ListaSubastasFragment();
                break;
            case 1: // Mis Pujas
                labelMisPujas.setTextColor(Color.parseColor("#1C2A21"));
                labelMisPujas.setTypeface(null, Typeface.BOLD);
                dotMisPujas.setVisibility(View.VISIBLE);
                if (homeHeaderSubtitle != null) {
                    homeHeaderSubtitle.setText("Mis Pujas");
                }
                selectedFragment = new MisPujasFragment();
                break;
            case 2: // Perfil (Ignorado por diseño actual, solo cambia el estado visual si se hace clic)
                labelPerfil.setTextColor(Color.parseColor("#1C2A21"));
                labelPerfil.setTypeface(null, Typeface.BOLD);
                dotPerfil.setVisibility(View.VISIBLE);
                if (homeHeaderSubtitle != null) {
                    homeHeaderSubtitle.setText("Mi Perfil");
                }
                // No cargamos ningún fragment para Perfil ya que no está implementado
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
