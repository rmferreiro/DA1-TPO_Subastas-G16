package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.response.SubastaResponse;
import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.enums.CategoriaUsuario;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.SubastaNoDisponibleException;
import ar.edu.uade.grupo16.subastas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

    public SubastaService(SubastaRepository subastaRepository,
                          AsistenteRepository asistenteRepository,
                          ClienteRepository clienteRepository,
                          UsuarioAuthRepository usuarioAuthRepository,
                          SesionSubastaRepository sesionSubastaRepository,
                          MedioPagoRepository medioPagoRepository,
                          MultaRepository multaRepository) {
        this.subastaRepository = subastaRepository;
        this.asistenteRepository = asistenteRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.sesionSubastaRepository = sesionSubastaRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.multaRepository = multaRepository;
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
     * Registra al cliente como asistente de la subasta.
     * Valida: estado abierta, categoría, multas pendientes, medio de pago verificado,
     * capacidad máxima y que no esté ya en otra subasta activa.
     */
    @Transactional
    public Map<String, Object> unirseASubasta(Integer subastaId, String emailCliente) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));

        // Verificar que esté abierta
        if (!"abierta".equalsIgnoreCase(subasta.getEstado())) {
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

        // Verificar medio de pago verificado
        if (!medioPagoRepository.existsByClienteIdentificadorAndVerificadoTrueAndActivoTrue(
                cliente.getIdentificador())) {
            throw new SubastaNoDisponibleException(
                    "Necesitás al menos un medio de pago verificado para participar.");
        }

        // Verificar capacidad
        long asistentesActuales = asistenteRepository.countBySubastaIdentificador(subastaId);
        if (subasta.getCapacidadAsistentes() != null &&
                asistentesActuales >= subasta.getCapacidadAsistentes()) {
            throw new SubastaNoDisponibleException("La subasta está llena");
        }

        // Verificar que no esté ya en otra subasta activa (1 subasta a la vez)
        if (sesionSubastaRepository.existsByClienteIdentificador(cliente.getIdentificador())) {
            SesionSubasta sesionActual = sesionSubastaRepository
                    .findByClienteIdentificador(cliente.getIdentificador()).get();
            if (!sesionActual.getSubasta().getIdentificador().equals(subastaId)) {
                throw new SubastaNoDisponibleException(
                        "Ya estás conectado a otra subasta. Salí primero de esa sala.");
            }
            // Ya está en esta subasta
            return Map.of(
                    "mensaje", "Ya estás en esta subasta",
                    "subastaId", subastaId,
                    "numeroPostor", asistenteRepository
                            .findByClienteIdentificadorAndSubastaIdentificador(
                                    cliente.getIdentificador(), subastaId)
                            .map(Asistente::getNumeroPostor).orElse(0)
            );
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

        // Registrar sesión activa
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
}
