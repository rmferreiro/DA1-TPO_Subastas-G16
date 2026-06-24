package ar.edu.uade.grupo16.subastas.config;

import ar.edu.uade.grupo16.subastas.repository.UsuarioAuthRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PasswordFixer implements CommandLineRunner {

    private final UsuarioAuthRepository usuarioAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordFixer(UsuarioAuthRepository usuarioAuthRepository, PasswordEncoder passwordEncoder) {
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        usuarioAuthRepository.findByEmail("juan.rodriguez@email.com").ifPresent(u -> {
            u.setPasswordHash(passwordEncoder.encode("uade"));
            usuarioAuthRepository.save(u);
            System.out.println("=========================================================");
            System.out.println("CONTRASEÑA DE JUAN RODRIGUEZ ACTUALIZADA A: 'uade'");
            System.out.println("=========================================================");
        });
    }
}
