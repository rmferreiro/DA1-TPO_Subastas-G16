package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.UsuarioAuth;
import ar.edu.uade.grupo16.subastas.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioAuthRepository extends JpaRepository<UsuarioAuth, Long> {
    Optional<UsuarioAuth> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UsuarioAuth> findByPersonaDocumento(String documento);
    List<UsuarioAuth> findByEstado(EstadoUsuario estado);
    Optional<UsuarioAuth> findByPersonaIdentificador(Integer personaId);
    Optional<UsuarioAuth> findByUuid(String uuid);
}
