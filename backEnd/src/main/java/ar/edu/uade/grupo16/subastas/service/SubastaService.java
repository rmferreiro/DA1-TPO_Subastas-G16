package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.response.EstadoVivoResponse;
import ar.edu.uade.grupo16.subastas.dto.response.SubastaResponse;
import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.enums.CategoriaUsuario;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.SubastaNoDisponibleException;
import ar.edu.uade.grupo16.subastas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final AsistenteRepository asistenteRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final SesionSubastaRepository sesionSubastaRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final MultaRepository multaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final PujoRepository pujoRepository;
    private final SubastadorRepository subastadorRepository;
    private final CatalogoRepository catalogoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProductoRepository productoRepository;
    private final PersonaRepository personaRepository;
    private final jakarta.persistence.EntityManager entityManager;

    public SubastaService(SubastaRepository subastaRepository,
                          AsistenteRepository asistenteRepository,
                          ClienteRepository clienteRepository,
                          UsuarioAuthRepository usuarioAuthRepository,
                          SesionSubastaRepository sesionSubastaRepository,
                          MedioPagoRepository medioPagoRepository,
                          MultaRepository multaRepository,
                          ItemCatalogoRepository itemCatalogoRepository,
                          PujoRepository pujoRepository,
                          SubastadorRepository subastadorRepository,
                          CatalogoRepository catalogoRepository,
                          EmpleadoRepository empleadoRepository,
                          ProductoRepository productoRepository,
                          PersonaRepository personaRepository,
                          jakarta.persistence.EntityManager entityManager) {
        this.subastaRepository = subastaRepository;
        this.asistenteRepository = asistenteRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.sesionSubastaRepository = sesionSubastaRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.multaRepository = multaRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.pujoRepository = pujoRepository;
        this.subastadorRepository = subastadorRepository;
        this.catalogoRepository = catalogoRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.personaRepository = personaRepository;
        this.entityManager = entityManager;
    }

    /**
     * Lista las subastas abiertas filtradas por categoría del cliente.
     * Un cliente de categoría X puede ver subastas de nivel X o inferior.
     */
    @Transactional(readOnly = true)
    public List<SubastaResponse> listarDisponibles(String emailCliente) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        CategoriaUsuario categoriaCliente = CategoriaUsuario.fromValor(cliente.getCategoria());

        return subastaRepository.findSubastasAbiertas()
                .stream()
                .filter(s -> s.getCategoria() != null)
                .filter(s -> {
                    try {
                        CategoriaUsuario catSubasta = CategoriaUsuario.fromValor(s.getCategoria());
                        // El cliente puede acceder si su nivel >= nivel de la subasta
                        return categoriaCliente.getNivel() >= catSubasta.getNivel();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(s -> toResponse(s, asistenteRepository.countBySubastaIdentificador(s.getIdentificador())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubastaResponse> listarTodas() {
        return subastaRepository.findAll()
                .stream()
                .map(s -> toResponse(s, asistenteRepository.countBySubastaIdentificador(s.getIdentificador())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubastaResponse getById(Integer subastaId) {
        Subasta s = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada: " + subastaId));
        return toResponse(s, asistenteRepository.countBySubastaIdentificador(subastaId));
    }

    /**
     * Devuelve el estado en tiempo real de una subasta:
     * item activo, mejor oferta actual, límites de la próxima puja
     * e historial de las últimas 10 pujas del item.
     * La app Android llama a esto al entrar a la sala y puede refrescarlo cada X segundos.
     */
    @Transactional(readOnly = true)
    public EstadoVivoResponse getEstadoVivo(Integer subastaId) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada: " + subastaId));

        // Items del catálogo de esta subasta
        List<ItemCatalogo> pendientes = itemCatalogoRepository
                .findByCatalogoSubastaIdentificadorAndSubastado(subastaId, "no");
        List<ItemCatalogo> vendidos = itemCatalogoRepository
                .findByCatalogoSubastaIdentificadorAndSubastado(subastaId, "si");

        // El item activo es el primero pendiente (menor ID = orden de catálogo)
        Optional<ItemCatalogo> itemActivo = pendientes.stream()
                .min((a, b) -> a.getIdentificador().compareTo(b.getIdentificador()));

        // Flag de categoría (para límite de puja)
        String catSubasta = subasta.getCategoria() != null ? subasta.getCategoria().toLowerCase() : "";
        boolean sinLimiteMaximo = catSubasta.equals("oro") || catSubasta.equals("platino");

        EstadoVivoResponse.ItemActivoInfo itemInfo = null;
        List<EstadoVivoResponse.PujaInfo> ultimasPujas = List.of();

        if (itemActivo.isPresent()) {
            ItemCatalogo item = itemActivo.get();
            BigDecimal precioBase = item.getPrecioBase();
            BigDecimal unPorciento = precioBase.multiply(new BigDecimal("0.01"));
            BigDecimal veintePorciento = precioBase.multiply(new BigDecimal("0.20"));

            // Mejor puja actual del item
            Optional<Pujo> mejorPuja = pujoRepository.findMejorPujaByItem(item.getIdentificador());
            BigDecimal mejorOferta = mejorPuja.map(Pujo::getImporte).orElse(null);
            String nombreMejorPostor = mejorPuja
                    .map(p -> p.getAsistente().getCliente().getPersona().getNombre())
                    .orElse(null);

            // Límites para la siguiente puja (Oro/Platino no tienen restricciones)
            BigDecimal siguienteMin;
            BigDecimal siguienteMax;
            if (sinLimiteMaximo) {
                siguienteMin = mejorOferta != null
                        ? mejorOferta.add(BigDecimal.ONE)
                        : precioBase;
                siguienteMax = null;
            } else {
                BigDecimal baseCalculo = mejorOferta != null ? mejorOferta : precioBase;
                siguienteMin = baseCalculo.add(unPorciento);
                siguienteMax = baseCalculo.add(veintePorciento);
            }

            // Datos de obra de arte (si aplica)
            String artista = null;
            String historia = null;
            if (item.getProducto().getProductoObraArte() != null) {
                artista  = item.getProducto().getProductoObraArte().getArtista();
                historia = item.getProducto().getProductoObraArte().getHistoria();
            }

            // Total de pujas sobre este item
            List<Pujo> todasLasPujas = pujoRepository
                    .findByItemIdentificadorOrderByFechaHoraAsc(item.getIdentificador());

            itemInfo = EstadoVivoResponse.ItemActivoInfo.builder()
                    .itemId(item.getIdentificador())
                    .productoId(item.getProducto().getIdentificador())
                    .orden(item.getOrden())
                    .descripcion(item.getProducto().getDescripcionCatalogo())
                    .descripcionCompleta(item.getProducto().getDescripcionCompleta())
                    .precioBase(precioBase)
                    .mejorOferta(mejorOferta)
                    .nombreMejorPostor(nombreMejorPostor)
                    .totalPujas(todasLasPujas.size())
                    .siguientePujaMinima(siguienteMin)
                    .siguientePujaMaxima(siguienteMax)
                    .sinLimiteMaximo(sinLimiteMaximo)
                    .artista(artista)
                    .historia(historia)
                    .build();

            // Últimas 10 pujas del item (más reciente primero)
            ultimasPujas = todasLasPujas.stream()
                    .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                    .limit(10)
                    .map(p -> EstadoVivoResponse.PujaInfo.builder()
                            .pujoId(p.getIdentificador())
                            .nombrePostor(p.getAsistente().getCliente().getPersona().getNombre())
                            .numeroPostor(p.getAsistente().getNumeroPostor())
                            .importe(p.getImporte())
                            .fechaHora(p.getFechaHora())
                            .esGanadora("si".equalsIgnoreCase(p.getGanador()))
                            .build())
                    .collect(Collectors.toList());
        }

        return EstadoVivoResponse.builder()
                .subastaId(subastaId)
                .estadoSubasta(subasta.getEstado())
                .moneda(subasta.getMoneda() != null ? subasta.getMoneda().name() : "ARS")
                .categoria(subasta.getCategoria())
                .itemsRestantes(pendientes.size())
                .itemsSubastados(vendidos.size())
                .limiteFinalizacionEpoch(subasta.getLimiteFinalizacionEpoch())
                .itemActual(itemInfo)
                .ultimasPujas(ultimasPujas)
                .build();
    }

    /**
     * Registra al cliente como asistente de la subasta.
     * Valida: estado abierta, categoría, multas pendientes, medio de pago verificado,
     * capacidad máxima y que no esté ya en otra subasta activa.
     */
    @Transactional
    public Map<String, Object> unirseASubasta(Integer subastaId, String emailCliente) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // Verificar que esté abierta
        if (!"ACTIVA".equalsIgnoreCase(subasta.getEstado())) {
            throw new SubastaNoDisponibleException("La subasta no está abierta");
        }

        UsuarioAuth auth = usuarioAuthRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        // Verificar categoría del cliente vs categoría de la subasta
        validarCategoriaAcceso(cliente, subasta);

        // Verificar multas pendientes
        if (multaRepository.existsByClienteIdentificadorAndPagadaFalse(cliente.getIdentificador())) {
            throw new SubastaNoDisponibleException(
                    "Tenés una multa pendiente. Debés pagarla antes de participar en subastas.");
        }

        // Eliminamos el bloqueo estricto de medio de pago para permitir Modo Espectador.
        // El chequeo se hará en el Frontend y al intentar procesar la puja.

        // Verificar capacidad
        long asistentesActuales = asistenteRepository.countBySubastaIdentificador(subastaId);
        if (subasta.getCapacidadAsistentes() != null &&
                asistentesActuales >= subasta.getCapacidadAsistentes()) {
            throw new SubastaNoDisponibleException("La subasta está llena");
        }

        // Verificar que no esté ya en otra subasta activa (1 subasta a la vez)
        Optional<SesionSubasta> sesionExistente = sesionSubastaRepository
                .findByClienteIdentificador(cliente.getIdentificador());
        if (sesionExistente.isPresent()) {
            SesionSubasta sesionActual = sesionExistente.get();
            if (sesionActual.getSubasta().getIdentificador().equals(subastaId)) {
                // Ya está en esta misma subasta — devolver su estado actual
                return Map.of(
                        "mensaje", "Ya estás en esta subasta",
                        "subastaId", subastaId,
                        "numeroPostor", asistenteRepository
                                .findByClienteIdentificadorAndSubastaIdentificador(
                                        cliente.getIdentificador(), subastaId)
                                .map(Asistente::getNumeroPostor).orElse(0)
                );
            }
            // La sesión es de otra subasta: verificar si esa subasta sigue ACTIVA
            String estadoAnterior = sesionActual.getSubasta().getEstado();
            if ("ACTIVA".equalsIgnoreCase(estadoAnterior)) {
                throw new SubastaNoDisponibleException(
                        "Ya estás en vivo en otra subasta. Salí primero de esa sala.");
            }
            // La subasta anterior ya finalizó o está pendiente → limpiar sesión obsoleta
            sesionSubastaRepository.deleteByClienteIdentificador(cliente.getIdentificador());
        }

        // Verificar que no sea ya asistente
        var asistenteExistente = asistenteRepository
                .findByClienteIdentificadorAndSubastaIdentificador(
                        cliente.getIdentificador(), subastaId);

        Asistente asistente;
        if (asistenteExistente.isPresent()) {
            asistente = asistenteExistente.get();
        } else {
            // Asignar número de postor (siguiente disponible)
            int numeroPostor = (int) asistentesActuales + 1;
            asistente = Asistente.builder()
                    .cliente(cliente)
                    .subasta(subasta)
                    .numeroPostor(numeroPostor)
                    .build();
            asistenteRepository.save(asistente);
        }

        // Registrar sesión activa — eliminar cualquier sesión anterior primero para evitar duplicados
        sesionSubastaRepository.deleteByClienteIdentificador(cliente.getIdentificador());
        SesionSubasta sesion = SesionSubasta.builder()
                .cliente(cliente)
                .subasta(subasta)
                .build();
        sesionSubastaRepository.save(sesion);

        return Map.of(
                "mensaje", "Te uniste a la subasta exitosamente",
                "subastaId", subastaId,
                "numeroPostor", asistente.getNumeroPostor()
        );
    }

    @Transactional
    public void salirDeSubasta(Integer subastaId, String emailCliente) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        sesionSubastaRepository.deleteByClienteIdentificador(auth.getPersona().getIdentificador());
    }

    private void validarCategoriaAcceso(Cliente cliente, Subasta subasta) {
        if (subasta.getCategoria() == null) return;
        try {
            CategoriaUsuario catCliente = CategoriaUsuario.fromValor(cliente.getCategoria());
            CategoriaUsuario catSubasta = CategoriaUsuario.fromValor(subasta.getCategoria());
            if (catCliente.getNivel() < catSubasta.getNivel()) {
                throw new SubastaNoDisponibleException(
                        "Tu categoría (" + cliente.getCategoria() + ") no permite acceder " +
                        "a esta subasta (requiere: " + subasta.getCategoria() + ")");
            }
        } catch (SubastaNoDisponibleException e) {
            throw e;
        } catch (Exception e) {
            throw new SubastaNoDisponibleException("Error al validar categoría de acceso");
        }
    }

    private SubastaResponse toResponse(Subasta s, long asistentesActuales) {
        return SubastaResponse.builder()
                .id(s.getIdentificador())
                .fecha(s.getFecha())
                .hora(s.getHora())
                .estado(s.getEstado())
                .categoria(s.getCategoria())
                .ubicacion(s.getUbicacion())
                .moneda(s.getMoneda() != null ? s.getMoneda().name() : "ARS")
                .descripcion(s.getDescripcion())
                .subastadorNombre(s.getSubastador() != null
                        ? s.getSubastador().getPersona().getNombre() : null)
                .capacidadAsistentes(s.getCapacidadAsistentes())
                .asistentesActuales((int) asistentesActuales)
                .tieneDeposito("si".equalsIgnoreCase(s.getTieneDeposito()))
                .seguridadPropia("si".equalsIgnoreCase(s.getSeguridadPropia()))
                .build();
    }

    @Transactional
    public Subasta crearSubasta(ar.edu.uade.grupo16.subastas.dto.request.SubastaRequest request) {
        // 0. Validar fecha y hora futura
        try {
            LocalDate requestFecha = LocalDate.parse(request.getFecha());
            LocalTime requestHora = LocalTime.parse(request.getHora());
            LocalDateTime fechaHoraSubasta = LocalDateTime.of(requestFecha, requestHora);
            
            long epochSubasta = fechaHoraSubasta.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long epochActual = Instant.now().toEpochMilli();
            
            if (epochSubasta < epochActual) {
                throw new ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException("La fecha es inválida, selecciona una posterior");
            }
        } catch (ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException("El formato de fecha u hora es inválido");
        }

        // 1. Buscar o crear Subastador por nombre
        String nombreRematador = request.getRematador().trim();
        Subastador subastador = subastadorRepository.findAll().stream()
                .filter(sub -> sub.getPersona() != null && nombreRematador.equalsIgnoreCase(sub.getPersona().getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    // Crear Persona temporal
                    Persona p = Persona.builder()
                            .nombre(nombreRematador)
                            .documento(String.valueOf((int) (Math.random() * 100000000)))
                            .direccion("Ubicación default")
                            .estado("activo")
                            .build();
                    p = personaRepository.save(p);

                    // Crear Subastador
                    Subastador sub = Subastador.builder()
                            .identificador(p.getIdentificador())
                            .persona(p)
                            .matricula("MAT-" + (int)(Math.random() * 9000 + 1000))
                            .region("General")
                            .build();
                    entityManager.persist(sub);
                    return sub;
                });

        Subasta subasta = Subasta.builder()
                .fecha(LocalDate.parse(request.getFecha()))
                .hora(LocalTime.parse(request.getHora()))
                .estado("PENDIENTE")
                .ubicacion(request.getUbicacion())
                .moneda(Moneda.valueOf(request.getMoneda().toUpperCase()))
                .categoria(request.getCategoria().toLowerCase())
                .descripcion(request.getDescripcion())
                .capacidadAsistentes(100)
                .tieneDeposito("si")
                .seguridadPropia("si")
                .subastador(subastador)
                .build();

        subasta = subastaRepository.save(subasta);

        // 3. Obtener Empleado para el Catálogo
        Empleado empleado = empleadoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay empleados registrados en el sistema para ser responsables del catálogo."));

        // 4. Crear Catálogo
        Catalogo catalogo = Catalogo.builder()
                .subasta(subasta)
                .responsable(empleado)
                .descripcion("Catálogo oficial para la subasta N° " + subasta.getIdentificador())
                .build();
        catalogoRepository.save(catalogo);

        // 5. Agregar Items al Catálogo
        int orden = 1;
        for (Integer productoId : request.getLotes()) {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

            BigDecimal comision = producto.getComisionPropuesta();
            if (comision == null || comision.compareTo(BigDecimal.valueOf(0.01)) <= 0) {
                comision = BigDecimal.valueOf(0.05); // 5% de comisión por defecto
            }

            ItemCatalogo item = ItemCatalogo.builder()
                    .catalogo(catalogo)
                    .producto(producto)
                    .precioBase(producto.getPrecioBasePropuesto())
                    .comision(comision)
                    .subastado("no")
                    .orden(orden++)
                    .build();
            itemCatalogoRepository.save(item);

            // Indicar que ya no está disponible y está en subasta
            producto.setDisponible("no");
            producto.setEstadoRevision("SUBASTANDO");
            productoRepository.save(producto);
        }

        return subasta;
    }
}
