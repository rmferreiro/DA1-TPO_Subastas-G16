package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.response.ClienteResponse;
import ar.edu.uade.grupo16.subastas.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Perfil y métricas del cliente autenticado")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del cliente autenticado")
    public ResponseEntity<ClienteResponse> getPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clienteService.getPerfil(userDetails.getUsername()));
    }

    @GetMapping("/metricas")
    @Operation(summary = "Obtener métricas de participación",
               description = "Total de pujas realizadas, victorias y tasa de éxito.")
    public ResponseEntity<Map<String, Object>> getMetricas(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clienteService.getMetricas(userDetails.getUsername()));
    }

    @GetMapping("/info-subasta")
    @Operation(summary = "Info previa a ingresar a una subasta",
               description = "Verifica si el cliente puede participar (multas, medios de pago).")
    public ResponseEntity<Map<String, Object>> getInfoParaSubasta(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clienteService.getInfoParaSubasta(userDetails.getUsername()));
    }
}
