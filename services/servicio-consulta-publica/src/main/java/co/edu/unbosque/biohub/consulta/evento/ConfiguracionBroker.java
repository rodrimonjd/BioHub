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

/**
 * Declara la cola propia de consulta pública, su cola de errores y el enlace al exchange de registro.
 * <ul>
 *   <li>Las dos colas son durables: si el servicio está apagado, los eventos esperan en RabbitMQ.</li>
 *   <li>Un mensaje que falla tras los reintentos (p. ej. la base de datos caída) se rechaza y RabbitMQ
 *       lo mueve solo a la cola de errores (dead-letter), en vez de perderlo.</li>
 * </ul>
 */
@Configuration
public class ConfiguracionBroker {

    @Bean
    public Declarables declaracionesEventos(@Value("${consulta.eventos.exchange}") String nombreExchange,
                                            @Value("${consulta.eventos.cola}") String nombreCola,
                                            @Value("${consulta.eventos.cola-errores}") String nombreColaErrores) {
        // mismos parámetros (durable, sin auto-borrado) con los que lo declara servicio-registro
        TopicExchange exchange = new TopicExchange(nombreExchange, true, false);
        Queue colaErrores = QueueBuilder.durable(nombreColaErrores).build();
        Queue cola = QueueBuilder.durable(nombreCola)
                .deadLetterExchange("")                 // exchange por defecto: enruta por nombre de cola
                .deadLetterRoutingKey(nombreColaErrores)
                .build();
        Binding registrados = BindingBuilder.bind(cola).to(exchange).with(EventoEntrante.HALLAZGO_REGISTRADO);
        Binding modificados = BindingBuilder.bind(cola).to(exchange).with(EventoEntrante.HALLAZGO_MODIFICADO);
        return new Declarables(exchange, colaErrores, cola, registrados, modificados);
    }
}
