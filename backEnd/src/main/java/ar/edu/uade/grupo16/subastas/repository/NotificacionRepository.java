package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    Page<Notificacion> findByClienteIdentificadorOrderByFechaCreacionDesc(Integer clienteId, Pageable pageable);

    long countByClienteIdentificadorAndLeidaFalse(Integer clienteId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.cliente.identificador = :clienteId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("clienteId") Integer clienteId);
}
