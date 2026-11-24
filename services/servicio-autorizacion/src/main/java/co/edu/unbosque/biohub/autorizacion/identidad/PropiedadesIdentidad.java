package co.edu.unbosque.biohub.autorizacion.identidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "identidad")
public record PropiedadesIdentidad(@NotNull Path claveArchivo, @NotBlank String emisor, @NotNull Duration duracionSesion,
                                   @NotNull Duration duracionConsumidor, String secretoServicios) {}
