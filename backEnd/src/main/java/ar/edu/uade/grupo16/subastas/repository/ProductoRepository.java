package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByDuenioIdentificador(Integer duenioId);
    List<Producto> findByDisponible(String disponible);
    List<Producto> findByEstadoRevision(String estadoRevision);
}
