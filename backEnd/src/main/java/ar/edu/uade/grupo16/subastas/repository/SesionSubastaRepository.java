package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.SesionSubasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SesionSubastaRepository extends JpaRepository<SesionSubasta, Long> {
    Optional<SesionSubasta> findByClienteIdentificador(Integer clienteId);
    boolean existsByClienteIdentificador(Integer clienteId);
    void deleteByClienteIdentificador(Integer clienteId);
    long countBySubastaIdentificador(Integer subastaId);
}
