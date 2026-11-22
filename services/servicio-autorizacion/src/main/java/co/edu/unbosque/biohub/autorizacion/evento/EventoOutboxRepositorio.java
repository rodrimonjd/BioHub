package co.edu.unbosque.biohub.autorizacion.evento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoOutboxRepositorio extends JpaRepository<EventoOutbox, Long> {
    List<EventoOutbox> findByPublicadoFalseOrderByIdAsc();
}
