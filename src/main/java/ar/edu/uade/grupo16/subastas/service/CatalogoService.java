package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException;
import ar.edu.uade.grupo16.subastas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogoService {

    private final CatalogoRepository catalogoRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ProductoRepository productoRepository;
    private final SubastaRepository subastaRepository;
    private final SeguroRepository seguroRepository;

    public CatalogoService(CatalogoRepository catalogoRepository,
                           ItemCatalogoRepository itemCatalogoRepository,
                           ProductoRepository productoRepository,
                           SubastaRepository subastaRepository,
                           SeguroRepository seguroRepository) {
        this.catalogoRepository = catalogoRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.productoRepository = productoRepository;
        this.subastaRepository = subastaRepository;
        this.seguroRepository = seguroRepository;
    }

    /**
     * Obtiene el catálogo completo de una subasta con todos sus items,
     * incluido el estado de cada item (subastado o no) y la mejor puja actual.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCatalogoDeSubasta(Integer subastaId) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada: " + subastaId));

        return catalogoRepository.findBySubastaIdentificador(subastaId)
                .stream()
                .flatMap(cat -> itemCatalogoRepository
                        .findByCatalogoIdentificador(cat.getIdentificador())
                        .stream()
                        .map(item -> buildItemResponse(item, cat)))
                .collect(Collectors.toList());
    }

    /**
     * Agrega un producto aprobado como item en un catálogo de una subasta.
     * Requiere precio base y comisión.
     * Valida que el producto tenga seguro vigente (requerimiento del enunciado).
     */
    @Transactional
    public Map<String, Object> agregarItemAlCatalogo(Integer subastaId, Map<String, Object> request) {
        Integer productoId = (Integer) request.get("productoId");
        BigDecimal precioBase = new BigDecimal(request.get("precioBase").toString());
        BigDecimal comision = new BigDecimal(
                request.getOrDefault("comision", "0").toString());
        Integer orden = (Integer) request.getOrDefault("orden", 1);

        // Validar que el producto exista y esté aprobado
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        if (!"ACEPTADO".equals(producto.getEstadoRevision())) {
            throw new RegistroInvalidoException(
                    "El producto debe estar en estado 'ACEPTADO' para ser subastado. " +
                    "Estado actual: " + producto.getEstadoRevision());
        }

        // Validar seguro vigente
        if (!seguroRepository.existsByProductoIdentificadorAndVigente(productoId, true)) {
            throw new RegistroInvalidoException(
                    "El producto debe tener un seguro vigente para poder ser subastado.");
        }

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // Obtener o crear catálogo de la subasta
        Catalogo catalogo = catalogoRepository.findBySubastaIdentificador(subastaId)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Catalogo nuevo = Catalogo.builder()
                            .subasta(subasta)
                            .build();
                    return catalogoRepository.save(nuevo);
                });

        // Verificar que no esté ya en el catálogo
        if (itemCatalogoRepository.existsByProductoIdentificadorAndCatalogoSubastaIdentificador(
                productoId, subastaId)) {
            throw new RegistroInvalidoException("Este producto ya está en el catálogo de esta subasta");
        }

        ItemCatalogo item = ItemCatalogo.builder()
                .catalogo(catalogo)
                .producto(producto)
                .precioBase(precioBase)
                .comision(comision)
                .orden(orden)
                .subastado("no")
                .build();
        item = itemCatalogoRepository.save(item);

        return Map.of(
                "mensaje", "Item agregado al catálogo",
                "itemId", item.getIdentificador(),
                "subastaId", subastaId,
                "productoId", productoId,
                "precioBase", precioBase,
                "comision", comision
        );
    }

    /**
     * Lista todos los items no subastados de la subasta (los que aún están disponibles).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getItemsDisponibles(Integer subastaId) {
        return itemCatalogoRepository.findByCatalogoSubastaIdentificadorAndSubastado(subastaId, "no")
                .stream()
                .map(item -> buildItemResponse(item,
                        catalogoRepository.findBySubastaIdentificador(subastaId)
                                .stream().findFirst().orElse(null)))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildItemResponse(ItemCatalogo item, Catalogo catalogo) {
        return Map.of(
                "itemId", item.getIdentificador(),
                "catalogoId", catalogo != null ? catalogo.getIdentificador() : "",
                "productoId", item.getProducto().getIdentificador(),
                "descripcion", item.getProducto().getDescripcionCompleta() != null
                        ? item.getProducto().getDescripcionCompleta() : "",
                "precioBase", item.getPrecioBase(),
                "comision", item.getComision() != null ? item.getComision() : BigDecimal.ZERO,
                "orden", item.getOrden() != null ? item.getOrden() : 0,
                "subastado", item.getSubastado()
        );
    }
}
