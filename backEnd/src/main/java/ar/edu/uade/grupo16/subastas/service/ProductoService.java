package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.enums.TipoNotificacion;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException;
import ar.edu.uade.grupo16.subastas.repository.*;
import java.util.Collections;
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
    private final ProductoObraArteRepository productoObraArteRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final RegistroSubastaRepository registroSubastaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           DuenioRepository duenioRepository,
                           FotoRepository fotoRepository,
                           SeguroRepository seguroRepository,
                           EmpleadoRepository empleadoRepository,
                           NotificacionService notificacionService,
                           ClienteRepository clienteRepository,
                           UsuarioAuthRepository usuarioAuthRepository,
                           MedioPagoRepository medioPagoRepository,
                           ProductoObraArteRepository productoObraArteRepository,
                           ItemCatalogoRepository itemCatalogoRepository,
                           RegistroSubastaRepository registroSubastaRepository) {
        this.productoRepository = productoRepository;
        this.duenioRepository = duenioRepository;
        this.fotoRepository = fotoRepository;
        this.seguroRepository = seguroRepository;
        this.empleadoRepository = empleadoRepository;
        this.notificacionService = notificacionService;
        this.clienteRepository = clienteRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.productoObraArteRepository = productoObraArteRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.registroSubastaRepository = registroSubastaRepository;
    }

    /**
     * Un dueño solicita ingresar un producto para que sea subastado.
     * El producto queda en estado 'pendiente' hasta que un empleado lo revise.
     */
    @Transactional
    public Map<String, Object> solicitarProducto(Map<String, Object> request, String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con email: " + email));
        
        Persona persona = auth.getPersona();
        
        // Creación de Dueño al vuelo si no existe
        Duenio duenio = duenioRepository.findById(persona.getIdentificador()).orElse(null);
        if (duenio == null) {
            Empleado verificador = empleadoRepository.findAll().stream().findFirst().orElse(null);
            duenio = Duenio.builder()
                    .persona(persona)
                    .pais(persona.getPais())
                    .verificacionFinanciera("si")
                    .verificacionJudicial("si")
                    .calificacionRiesgo(1)
                    .verificador(verificador)
                    .build();
            duenio = duenioRepository.save(duenio);
        }

        // Obtener primer verificador/empleado como revisor por defecto
        Empleado revisor = empleadoRepository.findAll().stream().findFirst().orElse(null);

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

        String tipo = (String) request.getOrDefault("tipo", "ESTANDAR");
        String subtitulo = (String) request.get("subtitulo");
        
        java.math.BigDecimal precioBase = java.math.BigDecimal.ZERO;
        if (request.containsKey("precioEstimado")) {
            precioBase = new java.math.BigDecimal(request.get("precioEstimado").toString());
        }

        String moneda = (String) request.getOrDefault("moneda", "ARS");

        String descLarga = (String) request.getOrDefault("descripcionLarga", "");
        String catalogoCombined = (subtitulo != null ? subtitulo : "") + " · " + descLarga;

        Producto producto = Producto.builder()
                .duenio(duenio)
                .revisor(revisor)
                .descripcionCompleta((String) request.get("descripcion"))
                .descripcionCatalogo(catalogoCombined)
                .tipoProducto(tipo)
                .declaracionPropiedad(true)
                .precioBasePropuesto(precioBase)
                .moneda(moneda)
                .estadoRevision("PENDIENTE")
                .disponible("no")
                .ubicacionDeposito("Depósito central · Buenos Aires")
                .build();
        producto = productoRepository.save(producto);

        // Si es OBRA_ARTE, registrar la entidad ProductoObraArte
        if ("OBRA_ARTE".equals(tipo)) {
            String artista = (String) request.get("artista");
            String historia = (String) request.get("historia");
            java.time.LocalDate fechaCreacion = null;
            if (request.containsKey("fechaCreacion")) {
                try {
                    fechaCreacion = java.time.LocalDate.parse((String) request.get("fechaCreacion"));
                } catch (Exception e) {
                    // Ignorar error de parsing
                }
            }
            
            ProductoObraArte obraArte = ProductoObraArte.builder()
                    .producto(producto)
                    .artista(artista)
                    .fechaCreacion(fechaCreacion)
                    .historia(historia)
                    .build();
            productoObraArteRepository.save(obraArte);
        }

        // Guardar fotos si vienen en Base64
        if (request.containsKey("fotos")) {
            @SuppressWarnings("unchecked")
            List<String> fotos = (List<String>) request.get("fotos");
            final Producto productoFinal = producto;
            fotos.stream().limit(6).forEach(fotoBase64 -> {
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
            if (request.containsKey("moneda")) {
                producto.setMoneda(request.get("moneda").toString());
            }
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
        if (estado == null) return java.util.Collections.emptyList();
        return productoRepository.findByEstadoRevision(estado.toUpperCase())
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        boolean tieneSeguro = seguroRepository.existsByProductoIdentificadorAndVigente(productoId, true);

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", p.getIdentificador());
        map.put("descripcion", p.getDescripcionCompleta() != null ? p.getDescripcionCompleta() : "");

        String rawCat = p.getDescripcionCatalogo() != null ? p.getDescripcionCatalogo() : "";
        String subtitulo = rawCat;
        String descLarga = "";
        int separatorIdx = rawCat.indexOf(" · ");
        if (separatorIdx != -1) {
            subtitulo = rawCat.substring(0, separatorIdx);
            descLarga = rawCat.substring(separatorIdx + 3);
        }
        map.put("subtitulo", subtitulo);
        map.put("descripcionCompleta", descLarga);

        map.put("estado", p.getEstadoRevision() != null ? p.getEstadoRevision() : "");
        map.put("duenioId", p.getDuenio() != null ? p.getDuenio().getIdentificador() : "");
        map.put("duenioNombre", p.getDuenio() != null && p.getDuenio().getPersona() != null ? p.getDuenio().getPersona().getNombre() : "Desconocido");
        map.put("tieneSeguroVigente", tieneSeguro);
        map.put("cantidadFotos", fotoRepository.countByProductoIdentificador(productoId));
        map.put("tipo", p.getTipoProducto() != null ? p.getTipoProducto() : "ESTANDAR");
        map.put("precioEstimado", p.getPrecioBasePropuesto() != null ? p.getPrecioBasePropuesto() : java.math.BigDecimal.ZERO);
        map.put("precioBasePropuesto", p.getPrecioBasePropuesto() != null ? p.getPrecioBasePropuesto() : java.math.BigDecimal.ZERO);
        map.put("comisionPropuesta", p.getComisionPropuesta() != null ? p.getComisionPropuesta() : java.math.BigDecimal.ZERO);
        map.put("moneda", p.getMoneda() != null ? p.getMoneda() : "ARS");
        map.put("ubicacionDeposito", p.getUbicacionDeposito() != null ? p.getUbicacionDeposito() : "");
        map.put("motivoRechazo", p.getMotivoRechazo() != null ? p.getMotivoRechazo() : "");

        // Información de venta: ¿fue vendido? ¿quién es el nuevo dueño?
        java.util.List<RegistroSubasta> regsVenta = registroSubastaRepository
                .findByProductoIdentificadorOrderByIdentificadorDesc(productoId);
        boolean vendido = !regsVenta.isEmpty();
        map.put("vendido", vendido);
        if (vendido) {
            RegistroSubasta ultimaVenta = regsVenta.get(0);
            String nombreNuevoDuenio = ultimaVenta.getCliente() != null
                    && ultimaVenta.getCliente().getPersona() != null
                    ? ultimaVenta.getCliente().getPersona().getNombre() : "Blackwood Subastas";
            map.put("nuevoDuenoNombre", nombreNuevoDuenio);
        } else {
            map.put("nuevoDuenoNombre", null);
        }

        java.util.List<String> fotosBase64 = fotoRepository.findByProductoIdentificador(productoId).stream()
                .map(f -> java.util.Base64.getEncoder().encodeToString(f.getFoto()))
                .collect(java.util.stream.Collectors.toList());
        map.put("fotos", fotosBase64);

        if ("OBRA_ARTE".equals(p.getTipoProducto()) && p.getProductoObraArte() != null) {
            ProductoObraArte oa = p.getProductoObraArte();
            map.put("artista", oa.getArtista() != null ? oa.getArtista() : "");
            map.put("fechaCreacion", oa.getFechaCreacion() != null ? oa.getFechaCreacion().toString() : "");
            map.put("historia", oa.getHistoria() != null ? oa.getHistoria() : "");
        }

        return map;
    }

    /**
     * Obtiene los bytes de la primera foto del producto (si existe).
     */
    @Transactional(readOnly = true)
    public byte[] getFotoPrincipal(Integer productoId) {
        return fotoRepository.findByProductoIdentificador(productoId).stream()
                .findFirst()
                .map(Foto::getFoto)
                .orElse(null);
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
                .map(p -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", p.getIdentificador());
                    map.put("descripcion", p.getDescripcionCompleta() != null ? p.getDescripcionCompleta() : "");
                    map.put("subtitulo", p.getDescripcionCatalogo() != null ? p.getDescripcionCatalogo() : "");
                    map.put("tipo", p.getTipoProducto() != null ? p.getTipoProducto() : "ESTANDAR");
                    map.put("estado", p.getEstadoRevision() != null ? p.getEstadoRevision() : "");
                    map.put("motivoRechazo", p.getMotivoRechazo() != null ? p.getMotivoRechazo() : "");
                    map.put("cuentaCobroConfigurada", p.getCuentaCobro() != null);
                    map.put("tieneSeguro", seguroRepository.existsByProductoIdentificadorAndVigente(p.getIdentificador(), true));
                    // Indicar si el producto ya fue vendido en subasta
                    java.util.List<RegistroSubasta> regs = registroSubastaRepository
                            .findByProductoIdentificadorOrderByIdentificadorDesc(p.getIdentificador());
                    map.put("vendido", !regs.isEmpty());
                    return map;
                })
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarProductosAprobados() {
        return productoRepository.findAll().stream()
                .filter(p -> ("ACEPTADO_DUENIO".equals(p.getEstadoRevision()) || "ACEPTADO".equals(p.getEstadoRevision())))
                .filter(p -> !itemCatalogoRepository.existsByProductoIdentificador(p.getIdentificador()))
                .map(p -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", p.getIdentificador());
                    map.put("descripcion", p.getDescripcionCompleta() != null ? p.getDescripcionCompleta() : "");
                    
                    // Extraer subtítulo
                    String rawCat = p.getDescripcionCatalogo() != null ? p.getDescripcionCatalogo() : "";
                    String subtitulo = rawCat;
                    int separatorIdx = rawCat.indexOf(" · ");
                    if (separatorIdx != -1) {
                        subtitulo = rawCat.substring(0, separatorIdx);
                    }
                    map.put("subtitulo", subtitulo);
                    map.put("precioBasePropuesto", p.getPrecioBasePropuesto() != null ? p.getPrecioBasePropuesto() : java.math.BigDecimal.ZERO);
                    map.put("moneda", p.getMoneda() != null ? p.getMoneda() : "ARS");
                    return map;
                })
                .collect(Collectors.toList());
    }
}
