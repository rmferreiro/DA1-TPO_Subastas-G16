package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.dto.request.PujaRequest;
import ar.edu.uade.grupo16.subastas.dto.response.PujaResponse;
import ar.edu.uade.grupo16.subastas.repository.ItemCatalogoRepository;
import ar.edu.uade.grupo16.subastas.repository.PujoRepository;
import ar.edu.uade.grupo16.subastas.service.PujaService;
import ar.edu.uade.grupo16.subastas.service.SubastaSalaManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subastas/{subastaId}/pujas")
@Tag(name = "Pujas", description = "Motor de pujas en vivo")
@SecurityRequirement(name = "bearerAuth")
public class PujaController {

    private final SubastaSalaManager salaManager;
    private final PujaService pujaService;
    private final PujoRepository pujoRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;

    public PujaController(SubastaSalaManager salaManager,
                          PujaService pujaService,
                          PujoRepository pujoRepository,
                          ItemCatalogoRepository itemCatalogoRepository) {
        this.salaManager = salaManager;
        this.pujaService = pujaService;
        this.pujoRepository = pujoRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
    }

    /**
     * Endpoint principal de pujas. La puja se encola y procesa asincrónicamente.
     * La respuesta llega por WebSocket en /topic/subasta/{subastaId}.
     */
    @PostMapping
    @Operation(summary = "Realizar una puja",
               description = "Encola la puja para ser procesada. El resultado llega por WebSocket.")
    public ResponseEntity<Map<String, Object>> pujar(
            @PathVariable Integer subastaId,
            @Valid @RequestBody PujaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        salaManager.encolarPuja(subastaId, request, userDetails.getUsername());

        return ResponseEntity.accepted().body(Map.of(
                "mensaje", "Puja recibida y procesándose",
                "subastaId", subastaId,
                "importe", request.getImporte()
        ));
    }

    /**
     * Cierre administrativo de un item (lo marca como vendido y genera el registro).
     * Normalmente lo llama el subastador.
     */
    @PostMapping("/items/{itemId}/cerrar")
    @Operation(summary = "[ADMIN] Cerrar puja de un item",
               description = "Cierra la licitación de un item, declara al ganador y registra la venta.")
    public ResponseEntity<PujaResponse> cerrarItem(
            @PathVariable Integer subastaId,
            @PathVariable Integer itemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PujaResponse resultado = pujaService.cerrarItem(subastaId, itemId, userDetails.getUsername());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Historial de pujas de un item.
     */
    @GetMapping("/items/{itemId}/historial")
    @Operation(summary = "Historial de pujas de un item")
    public ResponseEntity<List<Map<String, Object>>> historialItem(
            @PathVariable Integer subastaId,
            @PathVariable Integer itemId) {

        List<Map<String, Object>> historial = pujoRepository
                .findByItemIdentificadorOrderByFechaHoraAsc(itemId)
                .stream()
                .map(p -> Map.<String, Object>of(
                        "pujoId", p.getIdentificador(),
                        "numeroPostor", p.getAsistente().getNumeroPostor(),
                        "importe", p.getImporte(),
                        "ganador", p.getGanador(),
                        "fechaHora", p.getFechaHora()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(historial);
    }

    /**
     * Mejor puja actual de un item.
     */
    @GetMapping("/items/{itemId}/mejor")
    @Operation(summary = "Mejor puja actual de un item")
    public ResponseEntity<Map<String, Object>> mejorPuja(
            @PathVariable Integer subastaId,
            @PathVariable Integer itemId) {

        return pujoRepository.findMejorPujaByItem(itemId)
                .map(p -> ResponseEntity.ok(Map.<String, Object>of(
                        "importe", p.getImporte(),
                        "numeroPostor", p.getAsistente().getNumeroPostor(),
                        "fechaHora", p.getFechaHora()
                )))
                .orElse(ResponseEntity.ok(Map.of("mensaje", "Sin pujas aún",
                        "precioBase", itemCatalogoRepository.findById(itemId)
                                .map(i -> i.getPrecioBase()).orElse(null))));
    }
}
