package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.service.PujaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pujas")
@Tag(name = "Mis Pujas", description = "Historial de pujas del usuario")
@SecurityRequirement(name = "bearerAuth")
public class MisPujasController {

    private final PujaService pujaService;

    public MisPujasController(PujaService pujaService) {
        this.pujaService = pujaService;
    }

    @GetMapping("/mis-pujas")
    @Operation(summary = "Obtener mis pujas", description = "Devuelve el historial de pujas agrupadas por ítem")
    public ResponseEntity<List<Map<String, Object>>> getMisPujas(@AuthenticationPrincipal UserDetails userDetails) {
        List<Map<String, Object>> misPujas = pujaService.getMisPujas(userDetails.getUsername());
        return ResponseEntity.ok(misPujas);
    }

    @PostMapping("/items/{itemId}/pagar")
    @Operation(summary = "Confirmar pago de ítem ganado")
    public ResponseEntity<Map<String, Object>> pagarItem(
            @PathVariable Integer itemId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long medioPagoId = ((Number) request.get("medioPagoId")).longValue();
        Map<String, Object> result = pujaService.pagarItemGanado(itemId, medioPagoId, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }
}
