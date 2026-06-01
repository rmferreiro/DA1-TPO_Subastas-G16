package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.request.MedioPagoRequest;
import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import ar.edu.uade.grupo16.subastas.service.MedioPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medios-pago")
@Tag(name = "Medios de Pago", description = "Gestión de medios de pago del cliente")
@SecurityRequirement(name = "bearerAuth")
public class MedioPagoController {

    private final MedioPagoService medioPagoService;
    private final UsuarioAuthRepository usuarioAuthRepository;

    public MedioPagoController(MedioPagoService medioPagoService,
                               UsuarioAuthRepository usuarioAuthRepository) {
        this.medioPagoService = medioPagoService;
        this.usuarioAuthRepository = usuarioAuthRepository;
    }

    @PostMapping
    @Operation(summary = "Registrar medio de pago",
               description = "Registra un nuevo medio de pago para el cliente autenticado.")
    public ResponseEntity<Map<String, Object>> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MedioPagoRequest request) {

        Integer clienteId = getClienteId(userDetails);
        MedioPago mp = medioPagoService.registrar(clienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(medioPagoService.toResponseMap(mp));
    }

    @GetMapping
    @Operation(summary = "Listar mis medios de pago",
               description = "Devuelve todos los medios de pago activos del cliente autenticado.")
    public ResponseEntity<List<Map<String, Object>>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer clienteId = getClienteId(userDetails);
        List<Map<String, Object>> lista = medioPagoService.listarPorCliente(clienteId)
                .stream()
                .map(medioPagoService::toResponseMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar medio de pago",
               description = "Desactiva (soft delete) un medio de pago del cliente autenticado.")
    public ResponseEntity<Map<String, Object>> eliminar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Integer clienteId = getClienteId(userDetails);
        medioPagoService.desactivar(id, clienteId);
        return ResponseEntity.ok(Map.of("mensaje", "Medio de pago eliminado correctamente"));
    }

    // --- Endpoint admin para verificar un medio de pago ---
    @PutMapping("/admin/{id}/verificar")
    @Operation(summary = "[ADMIN] Verificar medio de pago")
    public ResponseEntity<Map<String, Object>> verificar(@PathVariable Long id) {
        medioPagoService.verificar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Medio de pago verificado"));
    }

    // --- Helper para obtener clienteId del usuario autenticado ---
    private Integer getClienteId(UserDetails userDetails) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return auth.getPersona().getIdentificador();
    }
}
