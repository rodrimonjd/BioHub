package co.edu.unbosque.biohub.reportes.evento;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import co.edu.unbosque.biohub.reportes.configuracion.PropiedadesReportes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(PropiedadesReportes propiedades) {
        String nombreExchange = propiedades.eventos().exchange();
        String nombreCola = propiedades.eventos().cola();
        String nombreColaErrores = propiedades.eventos().colaErrores();
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
