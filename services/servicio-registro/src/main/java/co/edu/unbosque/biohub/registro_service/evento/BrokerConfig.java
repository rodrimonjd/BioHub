package co.edu.unbosque.biohub.registro_service.evento;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerConfig {

    @Bean
    public TopicExchange exchangeEventos(@Value("${biohub.eventos.exchange}") String nombre) {
        return new TopicExchange(nombre, true, false);
    }
}
