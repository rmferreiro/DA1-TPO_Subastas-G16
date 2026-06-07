package tpo.g16.blackwood.login;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.register.RegisterActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * LoginActivity
 *
 * Pantalla de inicio de sesión de Blackwood Fine Sales.
 * Aparece inmediatamente después del SplashActivity.
 *
 * Funcionalidades:
 *  - Manejo del teclado virtual: usa WindowInsetsCompat para funcionar correctamente
 *    en Android 11+ donde adjustResize fue deprecado (Android 15 lo elimina por completo).
 *    El contenido sube de forma natural cuando el teclado aparece, centrando el campo activo.
 *  - Validación de formato de email con feedback visual en rojo.
 *  - Campo de contraseña con toggle de visibilidad (ojo) vía TextInputLayout.
 *  - "Crear cuenta nueva" navega a RegisterActivity.
 *  - Botones "Ingresar" y "¿Olvidaste tu contraseña?" clickeables (sin lógica aún).
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private TextView emailError;
    private Button btnIngresar;
    private Button btnCrearCuenta;
    private TextView forgotPassword;
    private ScrollView loginScrollView;
    private View loginHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ── Paso 1: Habilitar edge-to-edge ANTES de setContentView ──────────
        // Requerido para que WindowInsetsCompat reciba los insets del IME
        // (teclado virtual). Sin esto, en Android 11+ el teclado tapa los campos.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        setupKeyboardHandling();
        setupEmailValidation();
        setupButtons();
    }

    // ─────────────────────────────────────────────────────────
    // Binding de views
    // ─────────────────────────────────────────────────────────

    private void bindViews() {
        emailInput      = findViewById(R.id.login_email_input);
        emailError      = findViewById(R.id.login_email_error);
        btnIngresar     = findViewById(R.id.login_btn_ingresar);
        btnCrearCuenta  = findViewById(R.id.login_btn_crear_cuenta);
        forgotPassword  = findViewById(R.id.login_forgot_password);
        loginScrollView = findViewById(R.id.login_scroll_view);
        loginHeader     = findViewById(R.id.login_header);
    }

    // ─────────────────────────────────────────────────────────
    // Manejo del teclado virtual — IME Insets
    //
    //   La solución canónica para Android moderno:
    //   1. WindowCompat.setDecorFitsSystemWindows(false) → habilita edge-to-edge
    //      y permite que los insets del IME lleguen a los views.
    //   2. ViewCompat.setOnApplyWindowInsetsListener en el root view:
    //      - Ajusta paddingTop del header = altura de la status bar
    //        (para que el contenido no quede detrás de ella en edge-to-edge).
    //      - Ajusta paddingBottom del ScrollView = altura del teclado
    //        (cuando el teclado sube, el ScrollView se encoge y hace scroll
    //        automáticamente al campo enfocado).
    //   3. android:clipToPadding="false" en el ScrollView (layout) → permite
    //      que el contenido se vea durante el overscroll sin clipping.
    // ─────────────────────────────────────────────────────────

    private void setupKeyboardHandling() {
        View rootView = findViewById(R.id.login_root);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime        = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            // El header de la tarjeta queda debajo de la status bar
            // (paddingTop = altura de la status bar)
            loginHeader.setPadding(0, systemBars.top, 0, 0);

            // Si el teclado está visible (ime.bottom > 0), usamos bottomMargin para cambiar la altura
            // del ScrollView. Esto hace que Android automáticamente haga scroll al elemento enfocado.
            // Si el teclado no está visible, el margin es 0 y usamos paddingBottom para la barra de navegación.
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) loginScrollView.getLayoutParams();
            if (ime.bottom > 0) {
                params.bottomMargin = ime.bottom;
                loginScrollView.setPadding(0, 0, 0, 0);
            } else {
                params.bottomMargin = 0;
                loginScrollView.setPadding(0, 0, 0, systemBars.bottom);
            }
            loginScrollView.setLayoutParams(params);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    // ─────────────────────────────────────────────────────────
    // Validación de email (Regla 1)
    //   - Limpia el error mientras el usuario escribe.
    //   - Valida al perder el foco.
    //   - También valida al presionar "Ingresar".
    // ─────────────────────────────────────────────────────────

    private void setupEmailValidation() {
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (emailError.getVisibility() == View.VISIBLE) {
                    clearEmailError();
                }
            }
        });

        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });
    }

    /**
     * Valida el contenido del campo de email.
     * @return true si el email tiene formato válido.
     */
    private boolean validateEmail() {
        String email = emailInput.getText() != null
                ? emailInput.getText().toString().trim()
                : "";

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showEmailError();
            return false;
        }

        clearEmailError();
        return true;
    }

    private void showEmailError() {
        emailInput.setBackground(getDrawable(R.drawable.input_bg_error));
        emailError.setVisibility(View.VISIBLE);
    }

    private void clearEmailError() {
        emailInput.setBackground(getDrawable(R.drawable.input_bg));
        emailError.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────
    // Regla 2: Toggle de contraseña
    //   El ícono de ojo es manejado automáticamente por
    //   TextInputLayout con app:endIconMode="password_toggle".
    //   No se requiere lógica adicional aquí.
    // ─────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────
    // Botones y navegación
    // ─────────────────────────────────────────────────────────

    private void setupButtons() {
        btnIngresar.setOnClickListener(v -> {
            validateEmail();
            // TODO: implementar lógica de autenticación
        });

        // "Crear cuenta nueva" → primer paso del registro
        btnCrearCuenta.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        forgotPassword.setOnClickListener(v -> {
            // TODO: navegar a pantalla de recuperación de contraseña
        });
    }
}
