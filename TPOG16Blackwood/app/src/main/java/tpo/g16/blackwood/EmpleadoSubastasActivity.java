package tpo.g16.blackwood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpleadoSubastasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_subastas);

        // Botón nueva subasta
        findViewById(R.id.btn_nueva_subasta).setOnClickListener(v -> {
       //     Intent intent = new Intent(this, EmpleadoCrearSubastaActivity.class);
       //     startActivity(intent);
        });
    }
}