package tpo.g16.blackwood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MisPujasFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mis_pujas, container, false);

        // Ocultar el bottom_nav incluido en el layout, ya que HomeActivity tiene el suyo
        View bottomNav = view.findViewById(R.id.bottom_nav_include);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        } else {
            // Intento por si no tiene el ID explícito
            View tab = view.findViewById(R.id.tab_subastas);
            if (tab != null && tab.getParent() != null && tab.getParent() instanceof ViewGroup) {
                View parent = (View) tab.getParent();
                if (parent.getParent() != null && parent.getParent() instanceof ViewGroup) {
                    ((View) parent.getParent()).setVisibility(View.GONE);
                }
            }
        }

        // Click en Puja En Vivo Adjudicada
        View cardEnVivoGanada = view.findViewById(R.id.card_puja_envivo_ganada);
        if (cardEnVivoGanada != null) {
            cardEnVivoGanada.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new android.content.Intent(getActivity(), PermanecerSubastaActivity.class));
                }
            });
        }

        // Click en Puja Ganada
        View cardGanada = view.findViewById(R.id.card_puja_ganada);
        if (cardGanada != null) {
            cardGanada.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new android.content.Intent(getActivity(), NotificacionGanadorActivity.class));
                }
            });
        }

        // Click en Puja Perdida
        View cardPerdida = view.findViewById(R.id.card_puja_perdida);
        if (cardPerdida != null) {
            cardPerdida.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new android.content.Intent(getActivity(), NotificacionPerdedorActivity.class));
                }
            });
        }

        return view;
    }
}
