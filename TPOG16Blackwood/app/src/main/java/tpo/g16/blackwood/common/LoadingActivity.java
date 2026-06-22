package tpo.g16.blackwood.common;

import tpo.g16.blackwood.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESC = "extra_desc";
    public static final String EXTRA_INFO = "extra_info";
    public static final String EXTRA_NEXT_ACTIVITY = "extra_next_activity";
    public static final String EXTRA_DURATION = "extra_duration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Referencias
        TextView tvTitle = findViewById(R.id.tv_loading_title);
        TextView tvDesc = findViewById(R.id.tv_loading_desc);
        TextView tvInfo = findViewById(R.id.tv_loading_info);

        // Obtener datos del Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        String desc = intent.getStringExtra(EXTRA_DESC);
        String info = intent.getStringExtra(EXTRA_INFO);
        String nextActivityClassName = intent.getStringExtra(EXTRA_NEXT_ACTIVITY);
        int duration = intent.getIntExtra(EXTRA_DURATION, 3000);

        // Configurar textos si existen
        if (title != null) tvTitle.setText(title);
        if (desc != null) tvDesc.setText(desc);
        if (info != null) tvInfo.setText(info);
        
        // El subtitle del header se puede cambiar si es necesario
        View header = findViewById(R.id.include_header);
        TextView tvHeaderSubtitle = header.findViewById(R.id.header_subtitle);
        tvHeaderSubtitle.setText(getString(R.string.loading_procesando));

        // Simular proceso y navegar
        new Handler().postDelayed(() -> {
            if (nextActivityClassName != null) {
                try {
                    Class<?> nextActivity = Class.forName(nextActivityClassName);
                    Intent nextIntent = new Intent(LoadingActivity.this, nextActivity);
                    // Pasar datos extra si venían del registro (ej: nombre, apellido para el paso 2)
                    nextIntent.putExtras(intent); 
                    startActivity(nextIntent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            finish();
        }, duration);
    }
}
