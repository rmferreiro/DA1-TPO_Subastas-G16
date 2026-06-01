package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.service.CatalogoService;
import ar.edu.uade.grupo16.subastas.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión de productos y catálogos de subastas")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    private final ProductoService productoService;
    private final CatalogoService catalogoService;

    public ProductoController(ProductoService productoService, CatalogoService catalogoService) {
        this.productoService = productoService;
        this.catalogoService = catalogoService;
    }

    // ────────── Productos ──────────

    @PostMapping("/solicitar")
    @Operation(summary = "Solicitar ingreso de producto para subasta",
               description = "Un dueño solicita que su producto sea evaluado para ser subastado.")
    public ResponseEntity<Map<String, Object>> solicitar(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.solicitarProducto(request));
    }

    @GetMapping("/pendientes")
    @Operation(summary = "[EMPLEADO] Listar productos pendientes de revisión")
    public ResponseEntity<List<Map<String, Object>>> listarPendientes() {
        return ResponseEntity.ok(productoService.listarPorEstado("pendiente"));
    }

    @GetMapping("/aprobados")
    @Operation(summary = "Listar productos aprobados para subastar")
    public ResponseEntity<List<Map<String, Object>>> listarAprobados() {
        return ResponseEntity.ok(productoService.listarPorEstado("aprobado"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto (incluye seguro y fotos)")
    public ResponseEntity<Map<String, Object>> detalle(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.detalle(id));
    }

    @PutMapping("/{id}/revisar")
    @Operation(summary = "[EMPLEADO] Aprobar o rechazar un producto",
               description = "Decisión: 'aprobado' o 'rechazado'. Si rechazado, incluir motivo.")
    public ResponseEntity<Map<String, Object>> revisar(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(productoService.revisarProducto(id, request));
    }

    // ────────── Catálogos ──────────

    @GetMapping("/catalogo/{subastaId}")
    @Operation(summary = "Ver catálogo completo de una subasta")
    public ResponseEntity<List<Map<String, Object>>> getCatalogo(@PathVariable Integer subastaId) {
        return ResponseEntity.ok(catalogoService.getCatalogoDeSubasta(subastaId));
    }

    @GetMapping("/catalogo/{subastaId}/disponibles")
    @Operation(summary = "Items aún no subastados en la sesión actual")
    public ResponseEntity<List<Map<String, Object>>> getDisponibles(@PathVariable Integer subastaId) {
        return ResponseEntity.ok(catalogoService.getItemsDisponibles(subastaId));
    }

    @PostMapping("/catalogo/{subastaId}/items")
    @Operation(summary = "[EMPLEADO] Agregar producto al catálogo de una subasta",
               description = "Requiere precioBase y comisión. Valida seguro vigente.")
    public ResponseEntity<Map<String, Object>> agregarItem(
            @PathVariable Integer subastaId,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.agregarItemAlCatalogo(subastaId, request));
    }
}
