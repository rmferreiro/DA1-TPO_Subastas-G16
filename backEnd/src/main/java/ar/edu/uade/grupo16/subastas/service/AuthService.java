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
import java.util.List;
import java.util.Map;

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
    private final MedioPagoService medioPagoService;

    public AuthService(UsuarioAuthRepository usuarioAuthRepository,
                       PersonaRepository personaRepository,
                       ClienteRepository clienteRepository,
                       EmpleadoRepository empleadoRepository,
                       PaisRepository paisRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       MedioPagoService medioPagoService) {
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.paisRepository = paisRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.medioPagoService = medioPagoService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
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

        // 1. Crear Persona
        Persona persona = Persona.builder()
                .documento(request.getDocumento())
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .pais(pais)
                .estado("activo")
                .build();
        persona = personaRepository.save(persona);

        // 2. Crear Cliente — Inicialmente no admitido y sin categoría asignada (Etapa 1)
        Empleado verificador = empleadoRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        Cliente cliente = Cliente.builder()
                .persona(persona)
                .pais(pais)
                .admitido("no")          // No admitido aún (pendiente de aprobación)
                .categoria(null)         // Sin categoría hasta la aprobación externa
                .verificador(verificador)
                .build();
        clienteRepository.save(cliente);

        // 3. Crear UsuarioAuth en estado PENDIENTE y sin password real
        byte[] fotoFrente = null;
        byte[] fotoDorso  = null;
        if (request.getFotoDocFrente() != null && !request.getFotoDocFrente().isBlank()) {
            fotoFrente = java.util.Base64.getDecoder().decode(request.getFotoDocFrente());
        }
        if (request.getFotoDocDorso() != null && !request.getFotoDocDorso().isBlank()) {
            fotoDorso = java.util.Base64.getDecoder().decode(request.getFotoDocDorso());
        }

        UsuarioAuth auth = UsuarioAuth.builder()
                .persona(persona)
                .email(request.getEmail())
                .passwordHash("") // Vacío en etapa 1, se asigna al completar
                .estado(EstadoUsuario.PENDIENTE) // Estado PENDIENTE obligatoriamente
                .fotoDocFrente(fotoFrente)
                .fotoDocDorso(fotoDorso)
                .build();
        auth = usuarioAuthRepository.save(auth);

        return AuthResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .email(auth.getEmail())
                .nombre(persona.getNombre())
                .categoria(null)
                .estado(EstadoUsuario.PENDIENTE.name())
                .tokenType(null)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadoRegistro(String email) {
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RegistroInvalidoException("Usuario no encontrado"));
        return Map.of(
                "email", email,
                "estado", usuario.getEstado().name()
        );
    }

    @Transactional
    public Map<String, Object> aprobarUsuarioExterno(String email, String categoria) {
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RegistroInvalidoException("Usuario no encontrado"));
        
        usuario.setEstado(EstadoUsuario.APROBADO);
        usuarioAuthRepository.save(usuario);

        Cliente cliente = clienteRepository.findById(usuario.getPersona().getIdentificador())
                .orElseThrow(() -> new RegistroInvalidoException("Cliente no asociado a la persona"));
        cliente.setAdmitido("si");
        cliente.setCategoria(categoria.toLowerCase());
        clienteRepository.save(cliente);

        return Map.of(
                "mensaje", "Usuario aprobado con éxito",
                "email", email,
                "categoria", categoria
        );
    }

    @Transactional
    public Map<String, Object> rechazarUsuarioExterno(String email) {
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RegistroInvalidoException("Usuario no encontrado"));
        
        usuario.setEstado(EstadoUsuario.RECHAZADO);
        usuarioAuthRepository.save(usuario);

        Cliente cliente = clienteRepository.findById(usuario.getPersona().getIdentificador())
                .orElseThrow(() -> new RegistroInvalidoException("Cliente no asociado a la persona"));
        cliente.setAdmitido("no");
        clienteRepository.save(cliente);

        return Map.of(
                "mensaje", "Usuario rechazado con éxito",
                "email", email,
                "estado", EstadoUsuario.RECHAZADO.name()
        );
    }

    @Transactional
    public AuthResponse completarRegistro(String email, String password, List<ar.edu.uade.grupo16.subastas.dto.request.MedioPagoRequest> mediosPago) {
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RegistroInvalidoException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.APROBADO) {
            throw new RegistroInvalidoException("El usuario aún no ha sido aprobado por administración.");
        }

        // Asignar contraseña real hasheada
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuarioAuthRepository.save(usuario);

        Cliente cliente = clienteRepository.findById(usuario.getPersona().getIdentificador())
                .orElseThrow(() -> new RegistroInvalidoException("Cliente no encontrado"));

        // Registrar medios de pago si se envían
        if (mediosPago != null && !mediosPago.isEmpty()) {
            for (var mpRequest : mediosPago) {
                medioPagoService.registrar(cliente.getIdentificador(), mpRequest);
            }
        }

        // Generar tokens para iniciar sesión directamente
        String accessToken  = jwtTokenProvider.generateAccessToken(usuario.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(usuario.getEmail())
                .nombre(usuario.getPersona().getNombre())
                .categoria(cliente.getCategoria())
                .estado(usuario.getEstado().name())
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional
    public Map<String, Object> crearAdminDev() {
        String email = "admin@gmail.com";
        String plainPassword = "admin";
        
        // Buscar si ya existe
        var optUsuario = usuarioAuthRepository.findByEmail(email);
        if (optUsuario.isPresent()) {
            UsuarioAuth usuario = optUsuario.get();
            usuario.setEstado(EstadoUsuario.APROBADO);
            usuario.setPasswordHash(passwordEncoder.encode(plainPassword));
            usuarioAuthRepository.save(usuario);
            
            Cliente cliente = clienteRepository.findById(usuario.getPersona().getIdentificador())
                    .orElse(null);
            if (cliente != null) {
                cliente.setAdmitido("si");
                cliente.setCategoria("platino");
                clienteRepository.save(cliente);
            }
            
            return Map.of(
                    "mensaje", "Usuario administrador existente actualizado a password 'admin' y categoría 'platino'",
                    "email", email,
                    "estado", usuario.getEstado().name()
            );
        }

        // Obtener primer país disponible o fallback
        var pais = paisRepository.findById(1)
                .orElseGet(() -> paisRepository.findAll().stream().findFirst().orElse(null));

        // 1. Crear Persona
        Persona persona = Persona.builder()
                .documento("99999999")
                .nombre("Administrador Empresa")
                .direccion("Av. de la Administración 1")
                .pais(pais)
                .estado("activo")
                .build();
        persona = personaRepository.save(persona);

        // 2. Crear Empleado con cargo ADMINISTRADOR
        Empleado empleado = Empleado.builder()
                .persona(persona)
                .cargo("ADMINISTRADOR")
                .sector(null)
                .build();
        final Empleado empleadoPersistido = empleadoRepository.save(empleado);

        // Obtener primer verificador disponible, o el nuevo empleado en su defecto
        Empleado verificador = empleadoRepository.findAll().stream()
                .filter(e -> !e.getIdentificador().equals(empleadoPersistido.getIdentificador()))
                .findFirst()
                .orElse(empleadoPersistido);

        // 3. Crear Cliente Platino
        Cliente cliente = Cliente.builder()
                .persona(persona)
                .pais(pais)
                .admitido("si")
                .categoria("platino")
                .verificador(verificador)
                .build();
        clienteRepository.save(cliente);

        // 4. Crear UsuarioAuth
        UsuarioAuth auth = UsuarioAuth.builder()
                .persona(persona)
                .email(email)
                .passwordHash(passwordEncoder.encode(plainPassword))
                .estado(EstadoUsuario.APROBADO)
                .build();
        usuarioAuthRepository.save(auth);

        return Map.of(
                "mensaje", "Usuario administrador creado con éxito",
                "email", email,
                "password", plainPassword,
                "categoria", "platino",
                "cargo", "ADMINISTRADOR"
        );
    }
}
