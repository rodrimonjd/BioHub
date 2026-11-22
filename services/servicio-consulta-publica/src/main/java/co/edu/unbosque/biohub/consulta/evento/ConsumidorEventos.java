package co.edu.unbosque.biohub.consulta.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Escucha la cola de consulta pública (patrón Observer) y pasa cada evento al proyector. */
@Component
public class ConsumidorEventos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventos.class);

    private final JsonMapper json;
    private final ProyectorHallazgos proyector;

    public ConsumidorEventos(JsonMapper json, ProyectorHallazgos proyector) {
        this.json = json;
        this.proyector = proyector;
    }

    @RabbitListener(queues = "${consulta.eventos.cola}")
    public void recibir(Message mensaje) {
        EventoEntrante evento;
        try {
            evento = json.readValue(mensaje.getBody(), EventoEntrante.class);
        } catch (JacksonException e) {
            // un mensaje ilegible nunca se va a poder procesar: se descarta sin reintentos
            log.error("Mensaje descartado, JSON inválido: {}", e.getOriginalMessage());
            return;
        }
        proyector.aplicar(evento);
        log.info("Evento {} aplicado al hallazgo {}", evento.tipo(), evento.idAgregado());
    }
}
