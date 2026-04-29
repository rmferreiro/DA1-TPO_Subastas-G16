package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.response.NotificacionResponse;
import ar.edu.uade.grupo16.subastas.entity.Cliente;
import ar.edu.uade.grupo16.subastas.entity.Notificacion;
import ar.edu.uade.grupo16.subastas.enums.TipoNotificacion;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.repository.ClienteRepository;
import ar.edu.uade.grupo16.subastas.repository.NotificacionRepository;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               ClienteRepository clienteRepository,
                               UsuarioAuthRepository usuarioAuthRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificacionRepository = notificacionRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Crea una notificación persistente y la envía por WebSocket al usuario.
     */
    @Transactional
    public Notificacion crear(Cliente cliente, TipoNotificacion tipo,
                              String titulo, String mensaje,
                              Long referenciaId, String referenciaTipo) {
        Notificacion notif = Notificacion.builder()
                .cliente(cliente)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .referenciaId(referenciaId)
                .referenciaTipo(referenciaTipo)
                .fechaCreacion(LocalDateTime.now())
                .build();
        notif = notificacionRepository.save(notif);

        // Push en tiempo real al cliente vía WebSocket
        NotificacionResponse response = toResponse(notif);
        try {
            UsuarioAuthRepository repo = usuarioAuthRepository;
            repo.findByPersonaIdentificador(cliente.getIdentificador()).ifPresent(auth ->
                    messagingTemplate.convertAndSendToUser(
                            auth.getEmail(),
                            "/queue/notificaciones",
                            response
                    )
            );
        } catch (Exception e) {
            // Si falla el push WS, la notificación igual queda en DB
        }

        return notif;
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> listar(String email, int pagina, int tamanio) {
        var auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        var cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        Pageable pageable = PageRequest.of(pagina, tamanio);
        return notificacionRepository
                .findByClienteIdentificadorOrderByFechaCreacionDesc(cliente.getIdentificador(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> contarNoLeidas(String email) {
        var auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        var cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        long count = notificacionRepository
                .countByClienteIdentificadorAndLeidaFalse(cliente.getIdentificador());
        return Map.of("noLeidas", count);
    }

    @Transactional
    public void marcarTodasLeidas(String email) {
        var auth = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        var cliente = clienteRepository.findById(auth.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        notificacionRepository.marcarTodasComoLeidas(cliente.getIdentificador());
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .tipo(n.getTipo().name())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .leida(n.getLeida())
                .referenciaId(n.getReferenciaId())
                .referenciaTipo(n.getReferenciaTipo())
                .fechaCreacion(n.getFechaCreacion())
                .build();
    }
}
