package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.service.CatalogoService;
import ar.edu.uade.grupo16.subastas.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
               description = "Decisión: 'ACEPTADO' o 'RECHAZADO'. Si aceptado, incluir precioBase y comision. El estado pasará a PENDIENTE_DUENIO.")
    public ResponseEntity<Map<String, Object>> revisar(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(productoService.revisarProducto(id, request));
    }

<<<<<<< HEAD
    @GetMapping(value = "/{id}/foto", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Obtener foto principal de un producto en formato raw (bytes)")
    public ResponseEntity<byte[]> getFoto(@PathVariable Integer id) {
        byte[] foto = productoService.getFotoPrincipal(id);
        if (foto != null) {
            return ResponseEntity.ok(foto);
        }
        return ResponseEntity.notFound().build();
=======
    @PutMapping("/{id}/condiciones-duenio")
    @Operation(summary = "[DUEÑO] Aceptar o rechazar condiciones propuestas",
               description = "Decisión: 'ACEPTADO' o 'RECHAZADO'. Si el dueño acepta, el producto queda listo (ACEPTADO_DUENIO) para ir a catálogo.")
    public ResponseEntity<Map<String, Object>> responderCondiciones(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(productoService.responderCondiciones(id, request));
    }

    @PutMapping("/{id}/ubicacion")
    @Operation(summary = "[EMPLEADO] Actualizar ubicación del producto en depósito")
    public ResponseEntity<Map<String, Object>> actualizarUbicacion(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(productoService.actualizarUbicacion(id, request));
    }

    // ────────── Vendedores (Dueños) ──────────

    @GetMapping("/mis-productos")
    @Operation(summary = "[DUEÑO] Ver mis productos ofrecidos y su estado")
    public ResponseEntity<List<Map<String, Object>>> listarMisProductos(java.security.Principal principal) {
        return ResponseEntity.ok(productoService.listarMisProductos(principal.getName()));
    }

    @PutMapping("/{id}/cuenta-cobro")
    @Operation(summary = "[DUEÑO] Declarar cuenta bancaria para cobrar la venta",
               description = "Requiere el ID de un medioPago tipo CUENTA_BANCARIA ya verificado")
    public ResponseEntity<Map<String, Object>> declararCuentaCobro(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request,
            java.security.Principal principal) {
        Long medioPagoId = Long.valueOf(request.get("medioPagoId").toString());
        return ResponseEntity.ok(productoService.declararCuentaCobro(id, medioPagoId, principal.getName()));
    }

    @PostMapping("/{id}/seguro/aumentar")
    @Operation(summary = "[DUEÑO] Aumentar póliza de seguro abonando la diferencia")
    public ResponseEntity<Map<String, Object>> aumentarPoliza(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request,
            java.security.Principal principal) {
        java.math.BigDecimal montoAdicional = new java.math.BigDecimal(request.get("montoAdicional").toString());
        return ResponseEntity.ok(productoService.aumentarPoliza(id, montoAdicional, principal.getName()));
>>>>>>> ca6dc94214080f53249763627adfc6a129c21c2d
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

    @GetMapping("/catalogo/items/{itemId}")
    @Operation(summary = "Obtener detalles completos de un ítem del catálogo")
    public ResponseEntity<Map<String, Object>> getItemDetalle(@PathVariable Integer itemId) {
        return ResponseEntity.ok(catalogoService.getItemDetalle(itemId));
    }
}
