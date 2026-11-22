package co.edu.unbosque.biohub.registro_service.equipo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoCampoRepositorio extends JpaRepository<EquipoCampo, UUID> {
    boolean existsByNumeroSerie(String numeroSerie);
}