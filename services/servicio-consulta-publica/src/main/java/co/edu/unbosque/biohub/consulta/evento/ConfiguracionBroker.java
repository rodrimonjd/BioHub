package co.edu.unbosque.biohub.consulta.evento;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(@Value("${consulta.eventos.exchange}") String nombreExchange,
                                            @Value("${consulta.eventos.cola}") String nombreCola,
                                            @Value("${consulta.eventos.cola-errores}") String nombreColaErrores) {
        TopicExchange exchange = new TopicExchange(nombreExchange, true, false);
        Queue colaErrores = QueueBuilder.durable(nombreColaErrores).build();
        Queue cola = QueueBuilder.durable(nombreCola)
                .deadLetterExchange("")
                .deadLetterRoutingKey(nombreColaErrores)
                .build();
        Binding registrados = BindingBuilder.bind(cola).to(exchange).with(EventoEntrante.HALLAZGO_REGISTRADO);
        Binding modificados = BindingBuilder.bind(cola).to(exchange).with(EventoEntrante.HALLAZGO_MODIFICADO);
        return new Declarables(exchange, colaErrores, cola, registrados, modificados);
    }
}
