package tpo.g16.blackwood.splash;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.login.LoginActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import tpo.g16.blackwood.network.ApiConfig;

public class SplashActivity extends AppCompatActivity {

    // Durations (ms)
    private static final int PHASE1_DURATION = 700;   // Triángulos moviéndose al centro
    private static final int PHASE2_DELAY    = 200;   // Pausa entre fase 1 y 2
    private static final int PHASE2_DURATION = 400;   // Cross aparece
    private static final int PHASE3_DELAY    = 300;   // Pausa antes del texto
    private static final int TEXT_DURATION   = 500;   // Fade-in del texto
    private static final int HOLD_DURATION   = 1200;  // Tiempo en pantalla antes de pasar
    private static final int NEXT_DELAY      = 3500;  // Total antes de ir a MainActivity

    private SplashLogoView logoView;
    private TextView textTitle;
    private TextView textSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instalar la pantalla de splash oficial antes de super.onCreate
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Limpiar preferences al inicio para forzar relogin en cold starts
        SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Remover el splash del sistema inmediatamente cuando la actividad esté lista
        splashScreen.setOnExitAnimationListener(splashScreenProvider -> {
            splashScreenProvider.remove();
        });

        // Ocultar status bar para pantalla completa
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_splash);

        logoView   = findViewById(R.id.splash_logo);
        textTitle  = findViewById(R.id.splash_title);
        textSubtitle = findViewById(R.id.splash_subtitle);

        // Estado inicial: texto invisible
        textTitle.setAlpha(0f);
        textSubtitle.setAlpha(0f);

        startSplashAnimation();
    }

    private void startSplashAnimation() {

        // ── FASE 1: Triángulos vuelan al centro (700ms) ──────────────────
        logoView.post(() -> {
            AnimatorSet phase1 = logoView.buildPhase1Animator(PHASE1_DURATION);
            phase1.setInterpolator(new DecelerateInterpolator(1.8f));

            // ── FASE 2: Cruz dorada aparece (después de phase1 + delay) ──
            phase1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    logoView.postDelayed(() -> {
                        AnimatorSet phase2 = logoView.buildPhase2Animator(PHASE2_DURATION);
                        phase2.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {

                                // ── FASE 3: Texto fade-in ────────────────
                                logoView.postDelayed(() -> {
                                    ObjectAnimator titleFade = ObjectAnimator
                                            .ofFloat(textTitle, "alpha", 0f, 1f)
                                            .setDuration(TEXT_DURATION);

                                    ObjectAnimator subtitleFade = ObjectAnimator
                                            .ofFloat(textSubtitle, "alpha", 0f, 1f)
                                            .setDuration(TEXT_DURATION);
                                    subtitleFade.setStartDelay(120);

                                    ObjectAnimator titleY = ObjectAnimator
                                            .ofFloat(textTitle, "translationY", 24f, 0f)
                                            .setDuration(TEXT_DURATION);

                                    ObjectAnimator subtitleY = ObjectAnimator
                                            .ofFloat(textSubtitle, "translationY", 24f, 0f)
                                            .setDuration(TEXT_DURATION);
                                    subtitleY.setStartDelay(120);

                                    AnimatorSet textAnim = new AnimatorSet();
                                    textAnim.setInterpolator(new DecelerateInterpolator());
                                    textAnim.playTogether(titleFade, subtitleFade, titleY, subtitleY);
                                    textAnim.start();

                                }, PHASE3_DELAY);
                            }
                        });
                        phase2.start();
                    }, PHASE2_DELAY);
                }
            });
            phase1.start();
        });

        // ── Navegar a LoginActivity después del tiempo total ───────────────
        logoView.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, NEXT_DELAY);
    }
}
