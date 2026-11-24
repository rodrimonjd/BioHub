package co.edu.unbosque.biohub.reportes.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "reportes")
public record PropiedadesReportes(@Valid @NotNull Eventos eventos, @NotNull @Positive Long intervaloGeneracionMs,
                                  @Valid @NotNull Autorizacion autorizacion) {

    public record Eventos(@NotBlank String exchange, @NotBlank String cola, @NotBlank String colaErrores) {
    }

    public record Autorizacion(@NotNull URI url, @NotNull Duration cacheTtl) {
    }
}
