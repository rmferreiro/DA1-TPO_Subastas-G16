package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Pujo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PujoRepository extends JpaRepository<Pujo, Integer> {
    List<Pujo> findByItemIdentificadorOrderByFechaHoraAsc(Integer itemId);

    List<Pujo> findByAsistenteIdentificadorOrderByFechaHoraDesc(Integer asistenteId);

    @Query("SELECT p FROM Pujo p WHERE p.item.identificador = :itemId ORDER BY p.importe DESC")
    List<Pujo> findByItemOrderByImporteDesc(@Param("itemId") Integer itemId);

    @Query("SELECT p FROM Pujo p WHERE p.item.identificador = :itemId AND p.importe = " +
           "(SELECT MAX(p2.importe) FROM Pujo p2 WHERE p2.item.identificador = :itemId)")
    Optional<Pujo> findMejorPujaByItem(@Param("itemId") Integer itemId);

    @Query("SELECT p FROM Pujo p WHERE p.item.identificador = :itemId AND p.ganador = 'si'")
    Optional<Pujo> findGanadorByItem(@Param("itemId") Integer itemId);

    long countByAsistenteClienteIdentificador(Integer clienteId);

    @Query("SELECT COUNT(p) FROM Pujo p WHERE p.asistente.cliente.identificador = :clienteId AND p.ganador = 'si'")
    long countVictoriasByCliente(@Param("clienteId") Integer clienteId);
}
