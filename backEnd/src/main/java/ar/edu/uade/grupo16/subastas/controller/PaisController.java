package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.entity.Pais;
import ar.edu.uade.grupo16.subastas.repository.PaisRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paises")
@Tag(name = "Paises", description = "Listado de países para formularios")
public class PaisController {

    private final PaisRepository paisRepository;

    public PaisController(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener lista de países disponibles")
    public ResponseEntity<List<Pais>> listarPaises() {
        return ResponseEntity.ok(paisRepository.findAll());
    }
}
