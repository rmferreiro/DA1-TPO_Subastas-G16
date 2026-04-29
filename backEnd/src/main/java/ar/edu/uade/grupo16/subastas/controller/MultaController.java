package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import ar.edu.uade.grupo16.subastas.service.MultaService;
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
@RequestMapping("/api/multas")
@Tag(name = "Multas", description = "Gestión de multas por pujas no pagadas")
@SecurityRequirement(name = "bearerAuth")
public class MultaController {

    private final MultaService multaService;
    private final UsuarioAuthRepository usuarioAuthRepository;

    public MultaController(MultaService multaService,
                           UsuarioAuthRepository usuarioAuthRepository) {
        this.multaService = multaService;
        this.usuarioAuthRepository = usuarioAuthRepository;
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar multas pendientes de pago")
    public ResponseEntity<List<Map<String, Object>>> listarPendientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer clienteId = getClienteId(userDetails);
        return ResponseEntity.ok(multaService.listarMultasPendientes(clienteId));
    }

    @GetMapping
    @Operation(summary = "Listar todas las multas (pagadas y pendientes)")
    public ResponseEntity<List<Map<String, Object>>> listarTodas(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer clienteId = getClienteId(userDetails);
        return ResponseEntity.ok(multaService.listarTodasLasMultas(clienteId));
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Pagar una multa",
               description = "Registra el pago de la multa. Habilita al cliente a participar nuevamente.")
    public ResponseEntity<Map<String, Object>> pagar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer clienteId = getClienteId(userDetails);
        return ResponseEntity.ok(multaService.pagarMulta(id, clienteId));
    }

    // --- Admin: generar multa manualmente ---
    @PostMapping("/admin/generar")
    @Operation(summary = "[ADMIN] Generar multa manual",
               description = "Genera una multa para un cliente que no pagó su puja ganada.")
    public ResponseEntity<Map<String, Object>> generarManual(
            @RequestBody Map<String, Object> request) {
        Integer clienteId = (Integer) request.get("clienteId");
        Integer subastaId = (Integer) request.get("subastaId");
        Integer itemId = (Integer) request.get("itemId");
        var monto = new java.math.BigDecimal(request.get("montoOfertado").toString());

        var multa = multaService.generarMulta(clienteId, subastaId, itemId, monto);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Multa generada exitosamente",
                "multaId", multa.getId(),
                "montoMulta", multa.getMontoMulta(),
                "fechaLimite", multa.getFechaLimite()
        ));
    }

    private Integer getClienteId(UserDetails userDetails) {
        return usuarioAuthRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getPersona().getIdentificador();
    }
}
