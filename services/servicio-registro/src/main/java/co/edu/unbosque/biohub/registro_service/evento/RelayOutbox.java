package co.edu.unbosque.biohub.registro_service.evento;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RelayOutbox {

    private static final Logger log = LoggerFactory.getLogger(RelayOutbox.class);

    private final OutboxEventoRepository repositorio;
    private final PublicadorEventos publicador;
    private final int lote;

    public RelayOutbox(OutboxEventoRepository repositorio, PublicadorEventos publicador,
                       @Value("${biohub.eventos.lote}") int lote) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.lote = lote;
    }

    @Scheduled(fixedDelayString = "${biohub.eventos.intervalo-ms}")
    @Transactional
    public void publicarPendientes() {
        List<OutboxEvento> pendientes =
                repositorio.findByPublicadoFalseOrderByOcurrioEnAsc(Limit.of(lote));
        for (OutboxEvento evento : pendientes) {
            try {
                publicador.publicar(evento.getTipo(), evento.getPayload());
                evento.marcarPublicado();
            } catch (RuntimeException e) {
                evento.registrarFallo();
                log.warn("No se pudo publicar el evento {} ({}), intento {}: {}",
                        evento.getId(), evento.getTipo(), evento.getIntentos(), e.getMessage());
                break;
            }
        }
    }
}
