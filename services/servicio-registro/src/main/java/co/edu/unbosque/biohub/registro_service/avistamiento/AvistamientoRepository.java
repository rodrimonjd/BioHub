package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvistamientoRepository extends JpaRepository<Avistamiento, UUID> { }