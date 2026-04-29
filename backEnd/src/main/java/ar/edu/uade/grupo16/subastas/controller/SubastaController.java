package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.response.SubastaResponse;
import ar.edu.uade.grupo16.subastas.service.SubastaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subastas")
@Tag(name = "Subastas", description = "Listado, detalle e ingreso a subastas")
@SecurityRequirement(name = "bearerAuth")
public class SubastaController {

    private final SubastaService subastaService;

    public SubastaController(SubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @GetMapping
    @Operation(summary = "Listar subastas disponibles",
               description = "Devuelve las subastas abiertas accesibles para la categoría del cliente.")
    public ResponseEntity<List<SubastaResponse>> listarDisponibles(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subastaService.listarDisponibles(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de subasta")
    public ResponseEntity<SubastaResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(subastaService.getById(id));
    }

    @PostMapping("/{id}/unirse")
    @Operation(summary = "Unirse a una subasta",
               description = "Valida categoría, multas, medio de pago y capacidad. " +
                             "Solo se puede estar en 1 subasta a la vez.")
    public ResponseEntity<Map<String, Object>> unirse(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subastaService.unirseASubasta(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/salir")
    @Operation(summary = "Salir de una subasta",
               description = "Libera la sesión activa del cliente en la subasta indicada.")
    public ResponseEntity<Map<String, Object>> salir(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        subastaService.salirDeSubasta(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("mensaje", "Saliste de la subasta correctamente"));
    }

    // --- Endpoint admin para listar todas (sin filtrar por categoría) ---
    @GetMapping("/admin/todas")
    @Operation(summary = "[ADMIN] Listar todas las subastas")
    public ResponseEntity<List<SubastaResponse>> listarTodas() {
        return ResponseEntity.ok(subastaService.listarTodas());
    }
}
