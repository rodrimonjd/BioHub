package co.edu.unbosque.biohub.consulta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConsultaPublicaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultaPublicaApplication.class, args);
    }
}
