package co.edu.unbosque.biohub.historial.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "historial")
public record PropiedadesHistorial(@Valid @NotNull Eventos eventos, @NotNull URI autorizacionUrl,
                                   @NotBlank String permisoConsulta, @Valid @NotNull Cache cache) {

    public record Eventos(@NotBlank String exchange, @NotBlank String cola, @NotBlank String colaErrores) {
    }

    public record Cache(@NotNull Duration permisosTtl) {
    }
}
