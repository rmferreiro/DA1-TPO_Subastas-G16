package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.MedioPago;
import ar.edu.uade.grupo16.subastas.enums.Moneda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedioPagoRepository extends JpaRepository<MedioPago, Long> {
    List<MedioPago> findByClienteIdentificadorAndActivoTrue(Integer clienteId);
    List<MedioPago> findByClienteIdentificadorAndVerificadoTrueAndActivoTrue(Integer clienteId);
    List<MedioPago> findByClienteIdentificadorAndMonedaAndVerificadoTrueAndActivoTrue(Integer clienteId, Moneda moneda);
    boolean existsByClienteIdentificadorAndVerificadoTrueAndActivoTrue(Integer clienteId);
}
