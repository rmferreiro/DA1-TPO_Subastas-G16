package tpo.g16.blackwood.main;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.register.RegisterActivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Splash → Register
        new Handler().postDelayed(() -> {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);

            finish();

        }, 3000);
    }
}