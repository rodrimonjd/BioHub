package co.edu.unbosque.biohub.taxonomia;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaxonomiaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxonomiaServiceApplication.class, args);
    }
    
    @Bean
    public TopicExchange exchangeEventos(@Value("${biohub.eventos.exchange}") String nombre) {
        return new TopicExchange(nombre, true, false);
    }
}
