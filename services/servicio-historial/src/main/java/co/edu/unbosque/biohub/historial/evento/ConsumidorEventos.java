package co.edu.unbosque.biohub.historial.evento;

import co.edu.unbosque.biohub.historial.historial.ServicioRegistroHistorial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * Recibe los eventos de registro y taxonomía y los guarda en el historial.
 * Mensaje ilegible o incompleto: directo a la cola de errores. Otros fallos (p. ej. BD caída): reintentos y,
 * si siguen fallando, RabbitMQ lo pasa a la cola de errores. Ningún cambio se pierde en silencio.
 */
@Component
public class ConsumidorEventos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventos.class);
    static final String CABECERA_MOTIVO = "x-motivo-error";

    private final JsonMapper json;
    private final ServicioRegistroHistorial servicio;
    private final RabbitTemplate rabbit;
    private final String colaErrores;

    public ConsumidorEventos(JsonMapper json, ServicioRegistroHistorial servicio, RabbitTemplate rabbit,
                             @Value("${historial.eventos.cola-errores}") String colaErrores) {
        this.json = json;
        this.servicio = servicio;
        this.rabbit = rabbit;
        this.colaErrores = colaErrores;
    }

    @RabbitListener(queues = "${historial.eventos.cola}")
    public void recibir(Message mensaje) {
        String cuerpo = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        EventoEntrante evento;
        try {
            evento = json.readValue(cuerpo, EventoEntrante.class);
        } catch (JacksonException e) {
            aColaDeErrores(mensaje, "JSON inválido: " + e.getOriginalMessage());
            return;
        }
        try {
            ServicioRegistroHistorial.Resultado resultado = servicio.registrar(evento, cuerpo);
            log.info("Evento {} de {} {}", evento.tipo(), evento.idAgregado(),
                    resultado == ServicioRegistroHistorial.Resultado.GUARDADO ? "guardado en el historial" : "ignorado (repetido)");
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
