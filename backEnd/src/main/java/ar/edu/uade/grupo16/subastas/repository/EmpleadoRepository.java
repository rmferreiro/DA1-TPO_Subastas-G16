package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
}
