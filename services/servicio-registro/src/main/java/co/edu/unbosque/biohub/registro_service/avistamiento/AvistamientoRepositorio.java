package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AvistamientoRepositorio extends JpaRepository<Avistamiento, UUID> {
    
    List<Avistamiento> findAllByOrderByFechaHoraDesc();
}