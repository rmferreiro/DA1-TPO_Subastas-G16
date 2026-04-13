package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Catalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Integer> {
    List<Catalogo> findBySubastaIdentificador(Integer subastaId);
}
