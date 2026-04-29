package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.request.LoginRequest;
import ar.edu.uade.grupo16.subastas.dto.request.RegistroRequest;
import ar.edu.uade.grupo16.subastas.dto.response.AuthResponse;
import ar.edu.uade.grupo16.subastas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login y refresh de tokens")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario (Etapa 1)",
               description = "Registra datos personales + fotos del documento. El usuario queda PENDIENTE de aprobación.")
    public ResponseEntity<Map<String, Object>> registrar(@Valid @RequestBody RegistroRequest request) {
        String uuid = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Registro recibido exitosamente. Esperá el email de confirmación para poder ingresar.",
                "uuid", uuid
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
               description = "Login con email y password. Solo funciona para usuarios APROBADOS.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar access token",
               description = "Genera un nuevo access token usando el refresh token.")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
