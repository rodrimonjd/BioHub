package co.edu.unbosque.biohub.reportes.evento;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cola propia de reportes enlazada al exchange de eventos con AVISTAMIENTO_REGISTRADO.
 * Es durable: si reportes esta apagado, los avistamientos esperan en RabbitMQ.
 * Los mensajes que fallan tras los reintentos van a la cola de errores.
 */
@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(@Value("${reportes.eventos.exchange}") String nombreExchange,
                                            @Value("${reportes.eventos.cola}") String nombreCola,
                                            @Value("${reportes.eventos.cola-errores}") String nombreColaErrores) {
        TopicExchange exchange = new TopicExchange(nombreExchange, true, false);
        Queue colaErrores = QueueBuilder.durable(nombreColaErrores).build();
        Queue cola = QueueBuilder.durable(nombreCola)
                .deadLetterExchange("")
                .deadLetterRoutingKey(nombreColaErrores)
                .build();
        Binding avistamientos = BindingBuilder.bind(cola).to(exchange)
                .with(ConsumidorAvistamientos.AVISTAMIENTO_REGISTRADO);
        return new Declarables(exchange, colaErrores, cola, avistamientos);
    }
}
