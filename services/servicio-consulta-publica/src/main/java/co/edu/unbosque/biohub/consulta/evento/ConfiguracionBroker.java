package co.edu.unbosque.biohub.consulta.evento;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import co.edu.unbosque.biohub.consulta.configuracion.PropiedadesConsulta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(PropiedadesConsulta propiedades) {
        String nombreExchange = propiedades.eventos().exchange();
        String nombreCola = propiedades.eventos().cola();
        String nombreColaErrores = propiedades.eventos().colaErrores();
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
