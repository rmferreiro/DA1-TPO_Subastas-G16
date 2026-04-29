package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.request.LoginRequest;
import ar.edu.uade.grupo16.subastas.dto.request.RegistroRequest;
import ar.edu.uade.grupo16.subastas.dto.response.AuthResponse;
import ar.edu.uade.grupo16.subastas.entity.Cliente;
import ar.edu.uade.grupo16.subastas.entity.Empleado;
import ar.edu.uade.grupo16.subastas.entity.Persona;
import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.enums.EstadoUsuario;
import ar.edu.uade.grupo16.subastas.exception.RegistroInvalidoException;
import ar.edu.uade.grupo16.subastas.exception.UsuarioNoAprobadoException;
import ar.edu.uade.grupo16.subastas.repository.*;
import ar.edu.uade.grupo16.subastas.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
public class AuthService {

    private final UsuarioAuthRepository usuarioAuthRepository;
    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PaisRepository paisRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioAuthRepository usuarioAuthRepository,
                       PersonaRepository personaRepository,
                       ClienteRepository clienteRepository,
                       EmpleadoRepository empleadoRepository,
                       PaisRepository paisRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.paisRepository = paisRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public String registrar(RegistroRequest request) {
        // Validar que no exista el email
        if (usuarioAuthRepository.existsByEmail(request.getEmail())) {
            throw new RegistroInvalidoException("El email ya está registrado");
        }

        // Validar que no exista el documento
        if (personaRepository.existsByDocumento(request.getDocumento())) {
            throw new RegistroInvalidoException("El documento ya está registrado");
        }

        // Validar país
        var pais = paisRepository.findById(request.getPaisId())
                .orElseThrow(() -> new RegistroInvalidoException("País no válido"));

        // Obtener empleado del sistema como verificador por defecto
        Empleado verificador = empleadoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RegistroInvalidoException(
                        "No hay empleados en el sistema. Ejecute el script 03_datos_iniciales.sql"));

        // 1. Crear Persona
        Persona persona = Persona.builder()
                .documento(request.getDocumento())
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .estado("activo")
                .build();
        persona = personaRepository.save(persona);

        // 2. Crear Cliente (admitido='no', sin categoría hasta aprobación)
        Cliente cliente = Cliente.builder()
                .persona(persona)
                .pais(pais)
                .admitido("no")
                .categoria(null)
                .verificador(verificador)
                .build();
        clienteRepository.save(cliente);

        // 3. Crear UsuarioAuth con estado PENDIENTE
        byte[] fotoFrente = Base64.getDecoder().decode(request.getFotoDocFrente());
        byte[] fotoDorso = Base64.getDecoder().decode(request.getFotoDocDorso());

        UsuarioAuth auth = UsuarioAuth.builder()
                .persona(persona)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fotoDocFrente(fotoFrente)
                .fotoDocDorso(fotoDorso)
                .build();
        // @PrePersist generará UUID, fechaRegistro y estado PENDIENTE
        auth = usuarioAuthRepository.save(auth);

        return auth.getUuid();
    }

    public AuthResponse login(LoginRequest request) {
        // Verificar estado del usuario antes de intentar autenticación
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (usuario.getEstado() == EstadoUsuario.PENDIENTE) {
            throw new UsuarioNoAprobadoException(
                    "Tu cuenta está pendiente de aprobación. Esperá el email de confirmación.");
        }
        if (usuario.getEstado() == EstadoUsuario.RECHAZADO) {
            throw new UsuarioNoAprobadoException("Tu cuenta fue rechazada. No podés acceder al sistema.");
        }
        if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
            throw new UsuarioNoAprobadoException("Tu cuenta está bloqueada. Contactá a soporte.");
        }

        // Autenticar con Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

            // Obtener categoría del cliente
            String categoria = null;
            var cliente = clienteRepository.findById(usuario.getPersona().getIdentificador());
            if (cliente.isPresent()) {
                categoria = cliente.get().getCategoria();
            }

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(usuario.getEmail())
                    .nombre(usuario.getPersona().getNombre())
                    .categoria(categoria)
                    .estado(usuario.getEstado().name())
                    .tokenType("Bearer")
                    .build();
        } catch (DisabledException e) {
            throw new UsuarioNoAprobadoException("Tu cuenta no está habilitada para acceder.");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.APROBADO) {
            throw new UsuarioNoAprobadoException("Tu cuenta no está aprobada");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        String categoria = null;
        var cliente = clienteRepository.findById(usuario.getPersona().getIdentificador());
        if (cliente.isPresent()) {
            categoria = cliente.get().getCategoria();
        }

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(usuario.getEmail())
                .nombre(usuario.getPersona().getNombre())
                .categoria(categoria)
                .estado(usuario.getEstado().name())
                .tokenType("Bearer")
                .build();
    }
}
