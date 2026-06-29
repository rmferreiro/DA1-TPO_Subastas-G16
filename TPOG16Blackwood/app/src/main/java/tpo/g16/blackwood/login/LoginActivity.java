package tpo.g16.blackwood.login;

import tpo.g16.blackwood.R;
import tpo.g16.blackwood.register.RegisterActivity;
import tpo.g16.blackwood.main.HomeActivity;

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
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tpo.g16.blackwood.network.ApiConfig;
import tpo.g16.blackwood.network.RetrofitClient;
import tpo.g16.blackwood.network.model.AuthResponse;
import tpo.g16.blackwood.network.model.LoginRequest;
import tpo.g16.blackwood.network.model.ApiError;
import com.google.gson.Gson;

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
 *  - Botón "Ingresar" para autenticación.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private TextView emailError;
    private Button btnIngresar;
    private Button btnCrearCuenta;
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
        passwordInput   = findViewById(R.id.login_password_input);
        emailError      = findViewById(R.id.login_email_error);
        btnIngresar     = findViewById(R.id.login_btn_ingresar);
        btnCrearCuenta  = findViewById(R.id.login_btn_crear_cuenta);
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

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                passwordInput.setError(null);
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
        btnIngresar.setOnClickListener(v -> intentarLogin());

        // "Crear cuenta nueva" → primer paso del registro
        btnCrearCuenta.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void intentarLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        boolean isEmailValid = validateEmail();
        if (!isEmailValid) {
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("La contraseña es obligatoria");
            return;
        }

        btnIngresar.setEnabled(false);

        LoginRequest loginRequest = new LoginRequest(email, password);

        RetrofitClient.getInstance(this)
                .getAuthApiService()
                .login(loginRequest)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnIngresar.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();

                            SharedPreferences prefs = getSharedPreferences(ApiConfig.PREFS_NAME, MODE_PRIVATE);
                            prefs.edit()
                                    .putString(ApiConfig.KEY_ACCESS_TOKEN, auth.getAccessToken())
                                    .putString(ApiConfig.KEY_REFRESH_TOKEN, auth.getRefreshToken())
                                    .putString(ApiConfig.KEY_USER_EMAIL, auth.getEmail())
                                    .putString(ApiConfig.KEY_USER_NOMBRE, auth.getNombre())
                                    .putString(ApiConfig.KEY_USER_CATEGORIA, auth.getCategoria())
                                    .apply();

                            // Mostrar Toast de bienvenida
                            android.widget.Toast.makeText(LoginActivity.this, "Bienvenido " + auth.getNombre(), android.widget.Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            manejarErrorLogin(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnIngresar.setEnabled(true);
                        mostrarDialogoError("Error de conexión", "No se pudo conectar al servidor. Verifique su conexión a internet e intente de nuevo.");
                    }
                });
    }

    private void manejarErrorLogin(Response<?> response) {
        String serverMessage = null;
        try {
            if (response.errorBody() != null) {
                ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
                if (error != null) {
                    serverMessage = error.getMessage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response.code() == 401) {
            mostrarDialogoError("Credenciales incorrectas", 
                    "El email y/o contraseña no son correctos. Vuelva a intentarlo o restaure su contraseña.");
        } else if (response.code() == 403) {
            String msg = (serverMessage != null && !serverMessage.isEmpty()) 
                    ? serverMessage 
                    : "Tu cuenta no está autorizada para acceder al sistema.";
            mostrarDialogoError("Acceso no permitido", msg);
        } else {
            String msg = (serverMessage != null && !serverMessage.isEmpty()) 
                    ? serverMessage 
                    : "Ocurrió un error en el servidor (" + response.code() + "). Intente más tarde.";
            mostrarDialogoError("Error del servidor", msg);
        }
    }

    private void mostrarDialogoError(String titulo, String mensaje) {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
