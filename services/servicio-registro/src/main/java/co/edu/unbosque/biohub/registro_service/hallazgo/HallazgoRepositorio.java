package co.edu.unbosque.biohub.registro_service.hallazgo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HallazgoRepositorio extends JpaRepository<Hallazgo, UUID> { }