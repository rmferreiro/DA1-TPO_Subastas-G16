package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.enums.TipoNotificacion;
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
    private final NotificacionService notificacionService;
    private final ClienteRepository clienteRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final MedioPagoRepository medioPagoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           DuenioRepository duenioRepository,
                           FotoRepository fotoRepository,
                           SeguroRepository seguroRepository,
                           EmpleadoRepository empleadoRepository,
                           NotificacionService notificacionService,
                           ClienteRepository clienteRepository,
                           UsuarioAuthRepository usuarioAuthRepository,
                           MedioPagoRepository medioPagoRepository) {
        this.productoRepository = productoRepository;
        this.duenioRepository = duenioRepository;
        this.fotoRepository = fotoRepository;
        this.seguroRepository = seguroRepository;
        this.empleadoRepository = empleadoRepository;
        this.notificacionService = notificacionService;
        this.clienteRepository = clienteRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.medioPagoRepository = medioPagoRepository;
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

        Object declObj = request.get("declaracionJurada");
        boolean declaracionJurada = false;
        if (declObj instanceof Boolean) {
            declaracionJurada = (Boolean) declObj;
        } else if (declObj instanceof String) {
            declaracionJurada = Boolean.parseBoolean((String) declObj);
        }

        if (!declaracionJurada) {
            throw new RegistroInvalidoException("Debe declarar obligatoriamente que el bien le pertenece, su origen es lícito y no posee impedimento legal para subastarlo.");
        }

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
     * Si aprueba, indica precio base y comisión, y el producto pasa a PENDIENTE_DUENIO.
     * Si rechaza, pasa a RECHAZADO_EMPRESA.
     */
    @Transactional
    public Map<String, Object> revisarProducto(Integer productoId, Map<String, Object> request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        String decision = (String) request.get("decision"); // "ACEPTADO" o "RECHAZADO"
        if (!"ACEPTADO".equals(decision) && !"RECHAZADO".equals(decision)) {
            throw new RegistroInvalidoException("La decisión debe ser 'ACEPTADO' o 'RECHAZADO'");
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("productoId", productoId);

        if ("ACEPTADO".equals(decision)) {
            if (!request.containsKey("precioBase") || !request.containsKey("comision")) {
                throw new RegistroInvalidoException("Debe indicar precioBase y comision para aceptar el producto.");
            }
            BigDecimal precioBase = new BigDecimal(request.get("precioBase").toString());
            BigDecimal comision = new BigDecimal(request.get("comision").toString());

            producto.setPrecioBasePropuesto(precioBase);
            producto.setComisionPropuesta(comision);
            producto.setEstadoRevision("PENDIENTE_DUENIO");

            // Notificar al dueño para que revise las condiciones
            // El Dueño y Cliente comparten el mismo identificador (Persona)
            clienteRepository.findById(producto.getDuenio().getIdentificador()).ifPresent(cliente -> {
                notificacionService.crear(
                        cliente,
                        TipoNotificacion.PRODUCTO_ACEPTADO,
                        "Condiciones de subasta propuestas",
                        String.format("La empresa revisó '%s'. Precio base: $%.2f | Comisión: $%.2f. Revisá y aceptá para proceder.",
                                producto.getDescripcionCompleta(), precioBase, comision),
                        (long) producto.getIdentificador(), "PRODUCTO"
                );
            });

            response.put("estado", "PENDIENTE_DUENIO");
            response.put("mensaje", "Condiciones propuestas. Esperando aprobación del dueño.");
        } else {
            String motivoRechazo = (String) request.getOrDefault("motivo", "No especificado");
            producto.setEstadoRevision("RECHAZADO_EMPRESA");
            producto.setMotivoRechazo(motivoRechazo);

            response.put("estado", "RECHAZADO_EMPRESA");
            response.put("mensaje", "Producto rechazado por la empresa.");
            response.put("motivo", motivoRechazo);
        }

        productoRepository.save(producto);
        return response;
    }

    /**
     * El dueño acepta o rechaza las condiciones (precio base y comisión) propuestas por la empresa.
     */
    @Transactional
    public Map<String, Object> responderCondiciones(Integer productoId, Map<String, Object> request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        if (!"PENDIENTE_DUENIO".equals(producto.getEstadoRevision())) {
            throw new RegistroInvalidoException("El producto no está pendiente de confirmación de condiciones.");
        }

        String decision = (String) request.get("decision"); // "ACEPTADO" o "RECHAZADO"
        if (!"ACEPTADO".equals(decision) && !"RECHAZADO".equals(decision)) {
            throw new RegistroInvalidoException("La decisión debe ser 'ACEPTADO' o 'RECHAZADO'");
        }

        if ("ACEPTADO".equals(decision)) {
            producto.setEstadoRevision("ACEPTADO_DUENIO");
            productoRepository.save(producto);
            return Map.of("mensaje", "Condiciones aceptadas. El producto ya puede ser agregado a un catálogo.", "estado", "ACEPTADO_DUENIO");
        } else {
            producto.setEstadoRevision("RECHAZADO_DUENIO");
            productoRepository.save(producto);
            return Map.of("mensaje", "Condiciones rechazadas. Se coordinará la devolución con cargo.", "estado", "RECHAZADO_DUENIO");
        }
    }

    /**
     * Actualiza la ubicación física en depósito de un producto.
     */
    @Transactional
    public Map<String, Object> actualizarUbicacion(Integer productoId, Map<String, Object> request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        String nuevaUbicacion = (String) request.get("ubicacion");
        if (nuevaUbicacion == null || nuevaUbicacion.isBlank()) {
            throw new RegistroInvalidoException("La ubicación no puede estar vacía.");
        }

        producto.setUbicacionDeposito(nuevaUbicacion);
        productoRepository.save(producto);

        return Map.of(
                "mensaje", "Ubicación actualizada correctamente",
                "productoId", productoId,
                "ubicacion", nuevaUbicacion
        );
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

    /**
     * Lista los productos pertenecientes al usuario logueado (Panel Vendedor).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarMisProductos(String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        
        Integer duenioId = auth.getPersona().getIdentificador();
        
        return productoRepository.findAll().stream()
                .filter(p -> p.getDuenio() != null && p.getDuenio().getIdentificador().equals(duenioId))
                .map(p -> Map.<String, Object>of(
                        "id", p.getIdentificador(),
                        "descripcion", p.getDescripcionCompleta() != null ? p.getDescripcionCompleta() : "",
                        "estado", p.getEstadoRevision() != null ? p.getEstadoRevision() : "",
                        "motivoRechazo", p.getMotivoRechazo() != null ? p.getMotivoRechazo() : "",
                        "cuentaCobroConfigurada", p.getCuentaCobro() != null,
                        "tieneSeguro", seguroRepository.existsByProductoIdentificadorAndVigente(p.getIdentificador(), true)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Declara una cuenta bancaria (del dueño) para cobrar el resultado de la venta.
     */
    @Transactional
    public Map<String, Object> declararCuentaCobro(Integer productoId, Long medioPagoId, String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));
                
        if (!producto.getDuenio().getIdentificador().equals(auth.getPersona().getIdentificador())) {
            throw new RegistroInvalidoException("El producto no te pertenece.");
        }

        MedioPago mp = medioPagoRepository.findById(medioPagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medio de pago no encontrado"));

        if (!mp.getCliente().getIdentificador().equals(auth.getPersona().getIdentificador())) {
            throw new RegistroInvalidoException("El medio de pago no te pertenece.");
        }
        if (!Boolean.TRUE.equals(mp.getVerificado()) || !Boolean.TRUE.equals(mp.getActivo())) {
            throw new RegistroInvalidoException("La cuenta debe estar activa y verificada.");
        }
        if (mp.getTipo() != ar.edu.uade.grupo16.subastas.enums.TipoMedioPago.CUENTA_BANCARIA) {
            throw new RegistroInvalidoException("Solo podés declarar una CUENTA_BANCARIA para cobros.");
        }

        producto.setCuentaCobro(mp);
        productoRepository.save(producto);

        return Map.of("mensaje", "Cuenta de cobro vinculada exitosamente", "productoId", productoId, "medioPagoId", medioPagoId);
    }

    /**
     * Permite al cliente aumentar la póliza de seguro de su producto pagando la diferencia.
     */
    @Transactional
    public Map<String, Object> aumentarPoliza(Integer productoId, BigDecimal montoAdicional, String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
                
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));
                
        if (!producto.getDuenio().getIdentificador().equals(auth.getPersona().getIdentificador())) {
            throw new RegistroInvalidoException("El producto no te pertenece.");
        }

        Seguro seguro = seguroRepository.findByProductoIdentificadorAndVigente(productoId, true)
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no tiene un seguro vigente."));

        BigDecimal actual = seguro.getMontoCubierto() != null ? seguro.getMontoCubierto() : BigDecimal.ZERO;
        seguro.setMontoCubierto(actual.add(montoAdicional));
        seguroRepository.save(seguro);

        return Map.of(
                "mensaje", "Póliza aumentada exitosamente",
                "nuevoMontoCubierto", seguro.getMontoCubierto(),
                "diferenciaAbonada", montoAdicional
        );
    }
}
