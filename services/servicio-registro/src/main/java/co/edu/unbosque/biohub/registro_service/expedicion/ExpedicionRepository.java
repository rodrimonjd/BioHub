package co.edu.unbosque.biohub.registro_service.expedicion;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpedicionRepository extends JpaRepository<Expedicion, UUID> {

    List<Expedicion> findByLiderId(String liderId);
}