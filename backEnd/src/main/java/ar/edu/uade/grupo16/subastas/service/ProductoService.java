package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException;
import ar.edu.uade.grupo16.subastas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final DuenioRepository duenioRepository;
    private final FotoRepository fotoRepository;
    private final SeguroRepository seguroRepository;
    private final EmpleadoRepository empleadoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           DuenioRepository duenioRepository,
                           FotoRepository fotoRepository,
                           SeguroRepository seguroRepository,
                           EmpleadoRepository empleadoRepository) {
        this.productoRepository = productoRepository;
        this.duenioRepository = duenioRepository;
        this.fotoRepository = fotoRepository;
        this.seguroRepository = seguroRepository;
        this.empleadoRepository = empleadoRepository;
    }

    /**
     * Un dueño solicita ingresar un producto para que sea subastado.
     * El producto queda en estado 'pendiente' hasta que un empleado lo revise.
     */
    @Transactional
    public Map<String, Object> solicitarProducto(Map<String, Object> request) {
        Integer duenioId = (Integer) request.get("duenioId");
        Duenio duenio = duenioRepository.findById(duenioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dueño no encontrado: " + duenioId));

        Producto producto = Producto.builder()
                .duenio(duenio)
                .descripcionCompleta((String) request.get("descripcion"))
                .estadoRevision("PENDIENTE")
                .disponible("no")
                .build();
        producto = productoRepository.save(producto);

        // Guardar fotos si vienen en Base64
        if (request.containsKey("fotos")) {
            @SuppressWarnings("unchecked")
            List<String> fotos = (List<String>) request.get("fotos");
            final Integer productoId = producto.getIdentificador();
            final Producto productoFinal = producto;
            fotos.stream().limit(5).forEach(fotoBase64 -> {
                try {
                    Foto foto = Foto.builder()
                            .producto(productoFinal)
                            .foto(Base64.getDecoder().decode(fotoBase64))
                            .build();
                    fotoRepository.save(foto);
                } catch (Exception e) {
                    // Foto inválida, ignorar
                }
            });
        }

        return Map.of(
                "mensaje", "Producto enviado para revisión",
                "productoId", producto.getIdentificador(),
                "estado", "PENDIENTE"
        );
    }

    /**
     * Un empleado aprueba o rechaza un producto.
     * Si aprueba, debe indicar precio base y comisión.
     */
    @Transactional
    public Map<String, Object> revisarProducto(Integer productoId, Map<String, Object> request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        String decision = (String) request.get("decision"); // "ACEPTADO" o "RECHAZADO"
        if (!"ACEPTADO".equals(decision) && !"RECHAZADO".equals(decision)) {
            throw new RegistroInvalidoException("La decisión debe ser 'ACEPTADO' o 'RECHAZADO'");
        }

        producto.setEstadoRevision(decision);
        productoRepository.save(producto);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("productoId", productoId);
        response.put("estado", decision);

        if ("ACEPTADO".equals(decision)) {
            response.put("mensaje", "Producto aceptado. Ya puede ser asignado a un catálogo.");
        } else {
            String motivoRechazo = (String) request.getOrDefault("motivo", "No especificado");
            response.put("mensaje", "Producto rechazado");
            response.put("motivo", motivoRechazo);
        }

        return response;
    }

    /**
     * Lista productos por estado ('pendiente', 'aprobado', 'rechazado').
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorEstado(String estado) {
        return productoRepository.findByEstadoRevision(estado)
                .stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getIdentificador(),
                        "descripcion", p.getDescripcionCompleta() != null ? p.getDescripcionCompleta() : "",
                        "estado", p.getEstadoRevision() != null ? p.getEstadoRevision() : "",
                        "duenioId", p.getDuenio() != null ? p.getDuenio().getIdentificador() : null
                ))
                .collect(Collectors.toList());
    }

    /**
     * Detalle de un producto, incluyendo seguro vigente.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> detalle(Integer productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        // Verificar si tiene seguro vigente
        boolean tieneSeguro = seguroRepository.existsByProductoIdentificadorAndVigente(productoId, true);

        return Map.of(
                "id", p.getIdentificador(),
                "descripcion", p.getDescripcionCompleta() != null ? p.getDescripcionCompleta() : "",
                "estado", p.getEstadoRevision() != null ? p.getEstadoRevision() : "",
                "duenioId", p.getDuenio() != null ? p.getDuenio().getIdentificador() : "",
                "tieneSeguroVigente", tieneSeguro,
                "cantidadFotos", fotoRepository.countByProductoIdentificador(productoId)
        );
    }
}
