package co.edu.unbosque.biohub.registro_service.evento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class OutboxServicio {

    private final OutboxEventoRepositorio repositorio;
    private final JsonMapper json;

    public OutboxServicio(OutboxEventoRepositorio repositorio, JsonMapper json) {
        this.repositorio = repositorio;
        this.json = json;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void guardar(EventoDominio evento) {
        String payload = json.writeValueAsString(evento);
        repositorio.save(new OutboxEvento(evento.idEvento(), evento.tipo(), evento.idAgregado(), evento.ocurrioEn(), payload));
    }
}