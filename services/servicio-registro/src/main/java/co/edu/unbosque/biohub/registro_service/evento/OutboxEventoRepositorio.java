package co.edu.unbosque.biohub.registro_service.evento;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventoRepositorio extends JpaRepository<OutboxEvento, UUID> {

    List<OutboxEvento> findByPublicadoFalseOrderByOcurrioEnAsc(Limit limite);
}