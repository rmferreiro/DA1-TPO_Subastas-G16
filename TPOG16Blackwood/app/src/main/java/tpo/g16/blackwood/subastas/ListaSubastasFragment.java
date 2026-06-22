package tpo.g16.blackwood.subastas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tpo.g16.blackwood.R;

public class ListaSubastasFragment extends Fragment {

    private TextView chipTodas, chipOro, chipPlatino, chipDiamante;
    private View cardOro, cardPlatino, cardDiamante;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_lista_subastas, container, false);

        // Ocultar el bottom_nav que estaba en el layout antiguo (ya que ahora HomeActivity lo provee)
        View bottomNav = view.findViewById(R.id.bottom_nav_include);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        // Vincular chips
        chipTodas = view.findViewById(R.id.chip_todas);
        chipOro = view.findViewById(R.id.chip_oro);
        chipPlatino = view.findViewById(R.id.chip_platino);
        chipDiamante = view.findViewById(R.id.chip_diamante);

        // Vincular cards
        cardOro = view.findViewById(R.id.card_subasta_1);
        cardPlatino = view.findViewById(R.id.card_subasta_2);
        cardDiamante = view.findViewById(R.id.card_subasta_3);

        // Setup click listeners en chips para filtrar
        if (chipTodas != null) chipTodas.setOnClickListener(v -> selectFilter("todas"));
        if (chipOro != null) chipOro.setOnClickListener(v -> selectFilter("oro"));
        if (chipPlatino != null) chipPlatino.setOnClickListener(v -> selectFilter("platino"));
        if (chipDiamante != null) chipDiamante.setOnClickListener(v -> selectFilter("diamante"));

        // Setup click listeners en cards
        setupCardClick(cardOro);
        setupCardClick(cardPlatino);
        setupCardClick(cardDiamante);

        return view;
    }

    private void setupCardClick(View card) {
        if (card != null) {
            card.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), DetalleSubastaActivity.class));
                }
            });
        }
    }

    private void selectFilter(String category) {
        // Resetear estilos de todos los chips
        resetChipStyle(chipTodas);
        resetChipStyle(chipOro);
        resetChipStyle(chipPlatino);
        resetChipStyle(chipDiamante);

        // Resaltar el chip seleccionado y mostrar/ocultar las cards correspondientes
        switch (category) {
            case "todas":
                setChipSelected(chipTodas);
                setCardVisible(cardOro, true);
                setCardVisible(cardPlatino, true);
                setCardVisible(cardDiamante, true);
                break;
            case "oro":
                setChipSelected(chipOro);
                setCardVisible(cardOro, true);
                setCardVisible(cardPlatino, false);
                setCardVisible(cardDiamante, false);
                break;
            case "platino":
                setChipSelected(chipPlatino);
                setCardVisible(cardOro, false);
                setCardVisible(cardPlatino, true);
                setCardVisible(cardDiamante, false);
                break;
            case "diamante":
                setChipSelected(chipDiamante);
                setCardVisible(cardOro, false);
                setCardVisible(cardPlatino, false);
                setCardVisible(cardDiamante, true);
                break;
        }
    }

    private void resetChipStyle(TextView chip) {
        if (chip != null) {
            chip.setBackgroundResource(R.drawable.chip_outline_gold_border);
            chip.setTextColor(Color.parseColor("#6B6B6B"));
        }
    }

    private void setChipSelected(TextView chip) {
        if (chip != null) {
            chip.setBackgroundResource(R.drawable.chip_dark);
            chip.setTextColor(Color.parseColor("#F4F1EA"));
        }
    }

    private void setCardVisible(View card, boolean visible) {
        if (card != null) {
            card.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
