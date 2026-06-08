package tpo.g16.blackwood.subastas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tpo.g16.blackwood.R;

public class ListaSubastasFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_lista_subastas, container, false);

        // Ocultar el bottom_nav que estaba en el layout antiguo (ya que ahora HomeActivity lo provee)
        View bottomNav = view.findViewById(R.id.bottom_nav_include);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        // Card 1 → Detalle de subasta
        View cardSubasta1 = view.findViewById(R.id.card_subasta_1);
        if (cardSubasta1 != null) {
            cardSubasta1.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), DetalleSubastaActivity.class));
                }
            });
        }

        return view;
    }
}
