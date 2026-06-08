package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Acceso Público", description = "Endpoints de consulta libre sin autenticación")
public class PublicController {

    private final CatalogoService catalogoService;

    public PublicController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/catalogo/{subastaId}")
    @Operation(summary = "Ver catálogo público de una subasta", 
               description = "Retorna el catálogo sin incluir los precios base, ya que requieren estar registrado.")
    public ResponseEntity<List<Map<String, Object>>> getCatalogoPublico(@PathVariable Integer subastaId) {
        return ResponseEntity.ok(catalogoService.getCatalogoPublicoDeSubasta(subastaId));
    }
}
