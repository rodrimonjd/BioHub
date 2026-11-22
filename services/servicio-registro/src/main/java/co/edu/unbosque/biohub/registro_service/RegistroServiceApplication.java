package co.edu.unbosque.biohub.registro_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RegistroServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistroServiceApplication.class, args);
	}

}
