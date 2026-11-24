package co.edu.unbosque.biohub.historial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HistorialApplication {
    public static void main(String[] args) {
        SpringApplication.run(HistorialApplication.class, args);
    }
}
