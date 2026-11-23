package co.edu.unbosque.biohub.historial.evento;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(@Value("${historial.eventos.exchange}") String nombreExchange,
                                            @Value("${historial.eventos.cola}") String nombreCola,
                                            @Value("${historial.eventos.cola-errores}") String nombreColaErrores) {
        TopicExchange exchange = new TopicExchange(nombreExchange, true, false);
        Queue colaErrores = QueueBuilder.durable(nombreColaErrores).build();
        Queue cola = QueueBuilder.durable(nombreCola)
                .deadLetterExchange("")
                .deadLetterRoutingKey(nombreColaErrores)
                .build();
        List<Declarable> declaraciones = new ArrayList<>(List.of(exchange, colaErrores, cola));
        for (String tipo : EventoEntrante.TIPOS_ATENDIDOS) {
            declaraciones.add(BindingBuilder.bind(cola).to(exchange).with(tipo));
        }
        return new Declarables(declaraciones);
    }
}
