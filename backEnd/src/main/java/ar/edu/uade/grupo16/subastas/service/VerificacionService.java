package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.request.AprobacionRequest;
import ar.edu.uade.grupo16.subastas.entity.Cliente;
import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.enums.CategoriaUsuario;
import ar.edu.uade.grupo16.subastas.enums.EstadoUsuario;
import ar.edu.uade.grupo16.subastas.exception.RecursoNoEncontradoException;
import ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException;
import ar.edu.uade.grupo16.subastas.repository.ClienteRepository;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VerificacionService {

    private static final Logger log = LoggerFactory.getLogger(VerificacionService.class);

    private final UsuarioAuthRepository usuarioAuthRepository;
    private final ClienteRepository clienteRepository;
    private final MailService mailService;

    public VerificacionService(UsuarioAuthRepository usuarioAuthRepository,
                                ClienteRepository clienteRepository,
                                MailService mailService) {
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.clienteRepository = clienteRepository;
        this.mailService = mailService;
    }

    public List<Map<String, Object>> listarPendientes() {
        return usuarioAuthRepository.findByEstado(EstadoUsuario.PENDIENTE)
                .stream()
                .map(u -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("uuid", u.getUuid());
                    data.put("email", u.getEmail());
                    data.put("nombre", u.getPersona().getNombre());
                    data.put("documento", u.getPersona().getDocumento());
                    data.put("direccion", u.getPersona().getDireccion());
                    data.put("fechaRegistro", u.getFechaRegistro());
                    return data;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> aprobarUsuario(String uuid, AprobacionRequest request) {
        // Validar categoría
        CategoriaUsuario categoria;
        try {
            categoria = CategoriaUsuario.fromValor(request.getCategoria());
        } catch (IllegalArgumentException e) {
            throw new RegistroInvalidoException(
                    "Categoría no válida. Opciones: comun, especial, plata, oro, platino");
        }

        // Buscar usuario por UUID
        UsuarioAuth usuario = usuarioAuthRepository.findByUuid(uuid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con UUID: " + uuid));

        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new RegistroInvalidoException(
                    "El usuario no está en estado PENDIENTE. Estado actual: " + usuario.getEstado());
        }

        // Actualizar UsuarioAuth
        usuario.setEstado(EstadoUsuario.APROBADO);
        usuarioAuthRepository.save(usuario);

        // Actualizar Cliente
        Cliente cliente = clienteRepository.findById(usuario.getPersona().getIdentificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        cliente.setAdmitido("si");
        cliente.setCategoria(categoria.getValor());
        clienteRepository.save(cliente);

        // Enviar email de aprobación
        mailService.enviarEmailAprobacion(
                usuario.getEmail(),
                usuario.getPersona().getNombre(),
                categoria.getValor()
        );

        log.info("Usuario aprobado: {} (UUID: {}) con categoría: {}",
                usuario.getPersona().getNombre(), uuid, categoria.getValor());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario aprobado exitosamente");
        response.put("uuid", uuid);
        response.put("nombre", usuario.getPersona().getNombre());
        response.put("categoria", categoria.getValor());
        response.put("estado", EstadoUsuario.APROBADO.name());
        return response;
    }

    @Transactional
    public Map<String, Object> rechazarUsuario(String uuid) {
        UsuarioAuth usuario = usuarioAuthRepository.findByUuid(uuid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con UUID: " + uuid));

        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new RegistroInvalidoException(
                    "El usuario no está en estado PENDIENTE. Estado actual: " + usuario.getEstado());
        }

        // Actualizar estado
        usuario.setEstado(EstadoUsuario.RECHAZADO);
        usuarioAuthRepository.save(usuario);

        // Enviar email de rechazo
        mailService.enviarEmailRechazo(usuario.getEmail(), usuario.getPersona().getNombre());

        log.info("Usuario rechazado: {} (UUID: {})", usuario.getPersona().getNombre(), uuid);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario rechazado");
        response.put("uuid", uuid);
        response.put("nombre", usuario.getPersona().getNombre());
        response.put("estado", EstadoUsuario.RECHAZADO.name());
        return response;
    }
}
