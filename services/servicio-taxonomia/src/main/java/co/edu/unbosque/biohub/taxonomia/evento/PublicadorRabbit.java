package co.edu.unbosque.biohub.taxonomia.evento;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import co.edu.unbosque.biohub.taxonomia.configuracion.PropiedadesTaxonomia;
import org.springframework.stereotype.Component;

@Component
public class PublicadorRabbit implements PublicadorEventos {

    private final RabbitTemplate rabbit;
    private final String exchange;

    public PublicadorRabbit(RabbitTemplate rabbit, PropiedadesTaxonomia propiedades) {
        this.rabbit = rabbit;
        this.exchange = propiedades.eventos().exchange();
    }

    @Override
    public void publicar(String tipo, String payload) {
        MessageProperties propiedades = new MessageProperties();
        propiedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        propiedades.setContentEncoding("UTF-8");
        propiedades.setType(tipo);
        Message mensaje = new Message(payload.getBytes(StandardCharsets.UTF_8), propiedades);
        rabbit.send(exchange, tipo, mensaje);
    }
}
