package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Asistente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenteRepository extends JpaRepository<Asistente, Integer> {
    List<Asistente> findBySubastaIdentificador(Integer subastaId);
    List<Asistente> findByClienteIdentificador(Integer clienteId);
    Optional<Asistente> findByClienteIdentificadorAndSubastaIdentificador(Integer clienteId, Integer subastaId);
    long countBySubastaIdentificador(Integer subastaId);
}
