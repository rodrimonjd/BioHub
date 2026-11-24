package co.edu.unbosque.biohub.registro_service.evento;

import org.springframework.amqp.core.TopicExchange;
import co.edu.unbosque.biohub.registro_service.configuracion.PropiedadesRegistro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionBroker {

    @Bean
    public TopicExchange exchangeEventos(PropiedadesRegistro propiedades) {
        return new TopicExchange(propiedades.eventos().exchange(), true, false);
    }
}
