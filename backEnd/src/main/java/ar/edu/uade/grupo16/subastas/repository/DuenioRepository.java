package ar.edu.uade.grupo16.subastas.repository;

import ar.edu.uade.grupo16.subastas.entity.Duenio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DuenioRepository extends JpaRepository<Duenio, Integer> {
}
