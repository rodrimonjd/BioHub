package co.edu.unbosque.biohub.consulta.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Escucha la cola de consulta pública (patrón Observer) y pasa cada evento al proyector.
 * <ul>
 *   <li>Mensaje ilegible o con datos inválidos: va directo a la cola de errores, sin reintentos.</li>
 *   <li>Cualquier otro fallo (p. ej. base de datos caída): se reintenta y, si sigue fallando,
 *       RabbitMQ lo pasa a la cola de errores (dead-letter).</li>
 * </ul>
 */
@Component
public class ConsumidorEventos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventos.class);
    static final String CABECERA_MOTIVO = "x-motivo-error";

    private final JsonMapper json;
    private final ProyectorHallazgos proyector;
    private final RabbitTemplate rabbit;
    private final String colaErrores;

    public ConsumidorEventos(JsonMapper json, ProyectorHallazgos proyector, RabbitTemplate rabbit,
                             @Value("${consulta.eventos.cola-errores}") String colaErrores) {
        this.json = json;
        this.proyector = proyector;
        this.rabbit = rabbit;
        this.colaErrores = colaErrores;
    }

    @RabbitListener(queues = "${consulta.eventos.cola}")
    public void recibir(Message mensaje) {
        EventoEntrante evento;
        try {
            evento = json.readValue(mensaje.getBody(), EventoEntrante.class);
        } catch (JacksonException e) {
            aColaDeErrores(mensaje, "JSON inválido: " + e.getOriginalMessage());
            return;
        }
        try {
            ProyectorHallazgos.Resultado resultado = proyector.aplicar(evento);
            if (resultado == ProyectorHallazgos.Resultado.APLICADO) {
                log.info("Evento {} aplicado al hallazgo {}", evento.tipo(), evento.idAgregado());
            } else {
                log.info("Evento {} ignorado (repetido, antiguo o no aplica) para {}", evento.tipo(), evento.idAgregado());
            }
        } catch (EventoInvalidoException e) {
            aColaDeErrores(mensaje, e.getMessage());
        }
    }

    private void aColaDeErrores(Message mensaje, String motivo) {
        log.warn("Mensaje enviado a la cola de errores {}: {}", colaErrores, motivo);
        mensaje.getMessageProperties().setHeader(CABECERA_MOTIVO, motivo);
        rabbit.send("", colaErrores, mensaje);
    }
}
