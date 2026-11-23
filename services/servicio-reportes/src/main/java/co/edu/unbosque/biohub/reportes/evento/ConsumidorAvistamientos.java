package co.edu.unbosque.biohub.reportes.evento;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.edu.unbosque.biohub.reportes.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.reportes.avistamiento.AvistamientoRepositorio;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ConsumidorAvistamientos {

    public static final String AVISTAMIENTO_REGISTRADO = "AVISTAMIENTO_REGISTRADO";

    private static final Logger log = LoggerFactory.getLogger(ConsumidorAvistamientos.class);

    private final JsonMapper json;
    private final AvistamientoRepositorio avistamientos;

    public ConsumidorAvistamientos(JsonMapper json, AvistamientoRepositorio avistamientos) {
        this.json = json;
        this.avistamientos = avistamientos;
    }

    @RabbitListener(queues = "${reportes.eventos.cola}")
    public void recibir(Message mensaje) {
        EventoEntrante evento = json.readValue(mensaje.getBody(), EventoEntrante.class);
        procesar(evento);
    }

    void procesar(EventoEntrante evento) {
        if (!AVISTAMIENTO_REGISTRADO.equals(evento.tipo())) {
            return;
        }
        if (evento.idAgregado() == null || evento.datos() == null) {
            throw new IllegalArgumentException("Evento de avistamiento incompleto");
        }
        if (avistamientos.existsById(evento.idAgregado())) {
            log.info("Avistamiento {} ya estaba registrado; evento ignorado", evento.idAgregado());
            return;
        }
        Map<String, Object> d = evento.datos();
        Avistamiento avistamiento = new Avistamiento(
                evento.idAgregado(),
                texto(d, "especieId"),
                (String) d.get("especie"),
                fecha(d.get("fechaHora")),
                entero(d.get("cantidad")),
                texto(d, "departamento"),
                (String) d.get("region"));
        avistamientos.save(avistamiento);
        log.info("Avistamiento {} guardado para reportes", evento.idAgregado());
    }

    private static String texto(Map<String, Object> datos, String campo) {
        Object valor = datos.get(campo);
        if (valor == null || valor.toString().isBlank()) {
            throw new IllegalArgumentException("Falta el campo " + campo + " en el evento");
        }
        return valor.toString();
    }

    private static int entero(Object valor) {
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        throw new IllegalArgumentException("Falta la cantidad en el evento");
    }

    private static Instant fecha(Object valor) {
        if (valor instanceof String textoFecha) {
            return Instant.parse(textoFecha);
        }
        if (valor instanceof Number segundos) {
            double s = segundos.doubleValue();
            return Instant.ofEpochSecond((long) s, (long) ((s - Math.floor(s)) * 1_000_000_000));
        }
        throw new IllegalArgumentException("Falta la fechaHora en el evento");
    }

    public record EventoEntrante(String idEvento, String tipo, String idAgregado, Map<String, Object> datos) { }
}
