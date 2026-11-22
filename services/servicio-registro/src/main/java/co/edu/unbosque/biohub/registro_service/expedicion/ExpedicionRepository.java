package co.edu.unbosque.biohub.registro_service.expedicion;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpedicionRepository extends JpaRepository<Expedicion, UUID> {
}