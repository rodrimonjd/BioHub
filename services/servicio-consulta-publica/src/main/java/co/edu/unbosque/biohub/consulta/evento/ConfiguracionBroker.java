package co.edu.unbosque.biohub.consulta.evento;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara la cola propia de consulta pública y la enlaza al exchange de eventos de registro.
 * La cola es durable: si este servicio está apagado, los eventos esperan en RabbitMQ.
 */
@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(@Value("${consulta.eventos.exchange}") String nombreExchange,
                                            @Value("${consulta.eventos.cola}") String nombreCola) {
        // mismos parámetros (durable, sin auto-borrado) con los que lo declara servicio-registro
        TopicExchange exchange = new TopicExchange(nombreExchange, true, false);
        Queue cola = new Queue(nombreCola, true);
        Binding registrados = BindingBuilder.bind(cola).to(exchange).with(EventoEntrante.HALLAZGO_REGISTRADO);
        Binding modificados = BindingBuilder.bind(cola).to(exchange).with(EventoEntrante.HALLAZGO_MODIFICADO);
        return new Declarables(exchange, cola, registrados, modificados);
    }
}
