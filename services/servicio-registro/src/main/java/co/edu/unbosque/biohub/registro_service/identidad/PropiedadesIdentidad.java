package co.edu.unbosque.biohub.registro_service.identidad;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "identidad")
public record PropiedadesIdentidad(URI clavesUrl, String emisor, Duration espera) {}
