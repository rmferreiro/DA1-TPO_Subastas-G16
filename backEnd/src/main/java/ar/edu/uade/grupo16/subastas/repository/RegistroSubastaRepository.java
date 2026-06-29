package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.RegistroSubasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroSubastaRepository extends JpaRepository<RegistroSubasta, Integer> {
    List<RegistroSubasta> findBySubastaIdentificador(Integer subastaId);
    List<RegistroSubasta> findByClienteIdentificador(Integer clienteId);
    List<RegistroSubasta> findByDuenioIdentificador(Integer duenioId);
    List<RegistroSubasta> findByProductoIdentificadorOrderByIdentificadorDesc(Integer productoId);
    // Registros sin pago cuya ventana de 30 min ya expiró (excluye compras de empresa)
    List<RegistroSubasta> findByPagadoFalseAndCompraEmpresaFalseAndFechaCreacionBefore(LocalDateTime fechaLimite);
}
