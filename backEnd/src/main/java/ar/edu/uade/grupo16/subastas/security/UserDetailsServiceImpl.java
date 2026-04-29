package ar.edu.uade.grupo16.subastas.security;

import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.enums.EstadoUsuario;
import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioAuthRepository usuarioAuthRepository;

    public UserDetailsServiceImpl(UsuarioAuthRepository usuarioAuthRepository) {
        this.usuarioAuthRepository = usuarioAuthRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioAuth usuario = usuarioAuthRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Solo los usuarios APROBADOS pueden autenticarse
        boolean enabled = usuario.getEstado() == EstadoUsuario.APROBADO;

        String role = "ROLE_USER";
        if (usuario.getPersona() != null && usuario.getPersona().getEstado() != null) {
            // Se podría agregar lógica de roles más compleja aquí
        }

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                enabled,          // enabled
                true,             // accountNonExpired
                true,             // credentialsNonExpired
                true,             // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
