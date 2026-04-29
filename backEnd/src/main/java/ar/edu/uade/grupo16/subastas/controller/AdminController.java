package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.request.AprobacionRequest;
import ar.edu.uade.grupo16.subastas.service.VerificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración", description = "Aprobación y rechazo de usuarios (sin auth por ahora)")
public class AdminController {

    private final VerificacionService verificacionService;

    public AdminController(VerificacionService verificacionService) {
        this.verificacionService = verificacionService;
    }

    @GetMapping("/usuarios/pendientes")
    @Operation(summary = "Listar usuarios pendientes de aprobación",
               description = "Devuelve todos los usuarios con estado PENDIENTE y sus datos.")
    public ResponseEntity<List<Map<String, Object>>> listarPendientes() {
        return ResponseEntity.ok(verificacionService.listarPendientes());
    }

    @PutMapping("/usuarios/{uuid}/aprobar")
    @Operation(summary = "Aprobar usuario",
               description = "Aprueba un usuario por su UUID asignándole una categoría. Envía email automáticamente.")
    public ResponseEntity<Map<String, Object>> aprobarUsuario(
            @PathVariable String uuid,
            @Valid @RequestBody AprobacionRequest request) {
        return ResponseEntity.ok(verificacionService.aprobarUsuario(uuid, request));
    }

    @PutMapping("/usuarios/{uuid}/rechazar")
    @Operation(summary = "Rechazar usuario",
               description = "Rechaza un usuario por su UUID. El usuario no podrá hacer login.")
    public ResponseEntity<Map<String, Object>> rechazarUsuario(@PathVariable String uuid) {
        return ResponseEntity.ok(verificacionService.rechazarUsuario(uuid));
    }
}
