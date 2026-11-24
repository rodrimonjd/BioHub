package co.edu.unbosque.biohub.taxonomia.evento;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unbosque.biohub.taxonomia.configuracion.PropiedadesTaxonomia;

@Configuration
public class ConfiguracionBroker {

    @Bean
    public TopicExchange exchangeEventos(PropiedadesTaxonomia propiedades) {
        return new TopicExchange(propiedades.eventos().exchange(), true, false);
    }
}
