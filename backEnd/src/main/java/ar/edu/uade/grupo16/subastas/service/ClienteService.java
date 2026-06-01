package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.response.ClienteResponse;
import ar.edu.uade.grupo16.subastas.entity.Cliente;
import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.repository.ClienteRepository;
import ar.edu.uade.grupo16.subastas.repository.MedioPagoRepository;
import ar.edu.uade.grupo16.subastas.repository.PujoRepository;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final PujoRepository pujoRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          UsuarioAuthRepository usuarioAuthRepository,
                          MedioPagoRepository medioPagoRepository,
                          PujoRepository pujoRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.pujoRepository = pujoRepository;
    }

    @Transactional(readOnly = true)
    public ClienteResponse getPerfil(String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        boolean tieneMedioPago = medioPagoRepository
                .existsByClienteIdentificadorAndVerificadoTrueAndActivoTrue(cliente.getIdentificador());

        return ClienteResponse.builder()
                .id(cliente.getIdentificador())
                .nombre(auth.getPersona().getNombre())
                .documento(auth.getPersona().getDocumento())
                .direccion(auth.getPersona().getDireccion())
                .pais(cliente.getPais() != null ? cliente.getPais().getDescripcion() : null)
                .email(auth.getEmail())
                .categoria(cliente.getCategoria())
                .estado(auth.getEstado().name())
                .tieneMedioPagoVerificado(tieneMedioPago)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMetricas(String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Integer clienteId = auth.getPersona().getIdentificador();

        long totalPujas = pujoRepository.countByAsistenteClienteIdentificador(clienteId);
        long totalVictorias = pujoRepository.countVictoriasByCliente(clienteId);

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalPujas", totalPujas);
        metricas.put("totalVictorias", totalVictorias);
        metricas.put("tasaVictorias", totalPujas > 0
                ? String.format("%.1f%%", (double) totalVictorias / totalPujas * 100) : "0%");

        return metricas;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInfoParaSubasta(String email) {
        UsuarioAuth auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Cliente cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        boolean tieneMultasPendientes = false; // Se implementa en fase 4

        Map<String, Object> info = new HashMap<>();
        info.put("clienteId", cliente.getIdentificador());
        info.put("nombre", auth.getPersona().getNombre());
        info.put("categoria", cliente.getCategoria());
        info.put("admitido", cliente.getAdmitido());
        info.put("tieneMultasPendientes", tieneMultasPendientes);
        info.put("tieneMedioPagoVerificado",
                medioPagoRepository.existsByClienteIdentificadorAndVerificadoTrueAndActivoTrue(
                        cliente.getIdentificador()));
        return info;
    }
}
