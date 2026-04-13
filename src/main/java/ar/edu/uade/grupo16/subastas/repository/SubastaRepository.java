package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubastaRepository extends JpaRepository<Subasta, Integer> {
    List<Subasta> findByEstado(String estado);
    List<Subasta> findByCategoria(String categoria);
    List<Subasta> findByFechaGreaterThanEqual(LocalDate fecha);
    List<Subasta> findByEstadoAndCategoria(String estado, String categoria);

    @Query("SELECT s FROM Subasta s WHERE s.estado = 'abierta' ORDER BY s.fecha, s.hora")
    List<Subasta> findSubastasAbiertas();
}
