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
    @Operation(summary = "Registrar nuevo usuario",
               description = "Registra datos personales + fotos del documento. El usuario queda aprobado automáticamente con categoría 'común' y recibe sus tokens JWT de forma inmediata.")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @GetMapping("/registro/estado")
    @Operation(summary = "Obtener estado del trámite de registro",
               description = "Consulta el estado actual de aprobación del usuario mediante su email.")
    public ResponseEntity<Map<String, Object>> obtenerEstadoRegistro(@RequestParam String email) {
        return ResponseEntity.ok(authService.obtenerEstadoRegistro(email));
    }

    @PostMapping("/registro/aprobar")
    @Operation(summary = "[ADMIN/EXTERNO] Aprobar un usuario registrado",
               description = "Permite aprobar externamente un usuario y asignarle su categoría correspondiente.")
    public ResponseEntity<Map<String, Object>> aprobarUsuarioExterno(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String categoria = request.get("categoria");
        return ResponseEntity.ok(authService.aprobarUsuarioExterno(email, categoria));
    }

    @PostMapping("/registro/rechazar")
    @Operation(summary = "[ADMIN/EXTERNO] Rechazar un usuario registrado",
               description = "Permite rechazar externamente un usuario pendiente mediante su email.")
    public ResponseEntity<Map<String, Object>> rechazarUsuarioExterno(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return ResponseEntity.ok(authService.rechazarUsuarioExterno(email));
    }

    @PostMapping("/registro/completar")
    @Operation(summary = "Completar registro con contraseña y medios de pago",
               description = "Guarda la contraseña y opcionalmente los medios de pago para un usuario previamente aprobado.")
    public ResponseEntity<AuthResponse> completarRegistro(@Valid @RequestBody ar.edu.uade.grupo16.subastas.dto.request.CompletarRegistroRequest request) {
        AuthResponse response = authService.completarRegistro(request.getEmail(), request.getPassword(), request.getMediosPago());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dev/crear-admin")
    @Operation(summary = "[DEV] Crear/Actualizar usuario administrador de desarrollo",
               description = "Registra admin@gmail.com con contraseña 'admin', categoría platino y cargo de administrador.")
    public ResponseEntity<Map<String, Object>> crearAdminDev() {
        return ResponseEntity.ok(authService.crearAdminDev());
    }
}
