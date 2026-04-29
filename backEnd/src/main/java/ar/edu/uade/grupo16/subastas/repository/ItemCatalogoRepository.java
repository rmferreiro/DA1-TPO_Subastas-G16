package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.ItemCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, Integer> {
    List<ItemCatalogo> findByCatalogoIdentificador(Integer catalogoId);
    List<ItemCatalogo> findByCatalogoSubastaIdentificador(Integer subastaId);
    List<ItemCatalogo> findBySubastado(String subastado);
    List<ItemCatalogo> findByCatalogoSubastaIdentificadorAndSubastado(Integer subastaId, String subastado);
    boolean existsByProductoIdentificadorAndCatalogoSubastaIdentificador(Integer productoId, Integer subastaId);
}
