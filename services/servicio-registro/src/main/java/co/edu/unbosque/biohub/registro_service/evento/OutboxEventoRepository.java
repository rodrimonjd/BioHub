package co.edu.unbosque.biohub.registro_service.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventoRepository extends JpaRepository<OutboxEvento, UUID> { }