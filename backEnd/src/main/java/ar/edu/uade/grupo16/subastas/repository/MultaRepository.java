package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Multa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {
    List<Multa> findByClienteIdentificadorAndPagadaFalse(Integer clienteId);
    boolean existsByClienteIdentificadorAndPagadaFalse(Integer clienteId);
    List<Multa> findByClienteIdentificador(Integer clienteId);
    List<Multa> findByPagadaFalseAndDerivadoJusticiaFalseAndFechaLimiteBefore(java.time.LocalDateTime fechaLimite);
}
