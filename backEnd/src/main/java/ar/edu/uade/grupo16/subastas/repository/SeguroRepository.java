package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, String> {
    boolean existsByProductoIdentificadorAndVigente(Integer productoId, Boolean vigente);
    java.util.List<Seguro> findByProductoIdentificador(Integer productoId);
}
