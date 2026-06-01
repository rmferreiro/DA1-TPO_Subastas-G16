package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.response.NotificacionResponse;
import ar.edu.uade.grupo16.subastas.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Notificaciones del cliente")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones paginadas",
               description = "Devuelve las notificaciones del cliente más recientes primero.")
    public ResponseEntity<Page<NotificacionResponse>> listar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        return ResponseEntity.ok(notificacionService.listar(userDetails.getUsername(), pagina, tamanio));
    }

    @GetMapping("/no-leidas")
    @Operation(summary = "Cantidad de notificaciones no leídas")
    public ResponseEntity<Map<String, Object>> contarNoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificacionService.contarNoLeidas(userDetails.getUsername()));
    }

    @PutMapping("/marcar-leidas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public ResponseEntity<Map<String, Object>> marcarTodasLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificacionService.marcarTodasLeidas(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("mensaje", "Todas las notificaciones marcadas como leídas"));
    }
}
