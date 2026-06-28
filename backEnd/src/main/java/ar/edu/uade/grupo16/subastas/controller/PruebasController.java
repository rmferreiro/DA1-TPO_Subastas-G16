package ar.edu.uade.grupo16.subastas.controller;

import ar.edu.uade.grupo16.subastas.repository.ItemCatalogoRepository;
import ar.edu.uade.grupo16.subastas.repository.PujoRepository;
import ar.edu.uade.grupo16.subastas.repository.RegistroSubastaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pruebas")
@Tag(name = "Pruebas", description = "Endpoints de utilidad para testing")
public class PruebasController {

    private final PujoRepository pujoRepository;
    private final RegistroSubastaRepository registroSubastaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;

    public PruebasController(PujoRepository pujoRepository,
                             RegistroSubastaRepository registroSubastaRepository,
                             ItemCatalogoRepository itemCatalogoRepository) {
        this.pujoRepository = pujoRepository;
        this.registroSubastaRepository = registroSubastaRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
    }

    @GetMapping("/reset")
    @Transactional
    public ResponseEntity<String> resetearSubasta() {
        // Borrar el historial de pujas y registros de ventas
        pujoRepository.deleteAllInBatch();
        registroSubastaRepository.deleteAllInBatch();

        // Volver a poner disponibles todos los items de la base de datos
        itemCatalogoRepository.findAll().forEach(item -> {
            item.setSubastado("no");
            itemCatalogoRepository.save(item);
        });

        return ResponseEntity.ok("Base de datos de pujas reseteada correctamente. Ya podés volver a probar.");
    }
}
