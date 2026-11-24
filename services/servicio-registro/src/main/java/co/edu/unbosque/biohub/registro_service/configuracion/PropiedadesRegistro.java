package co.edu.unbosque.biohub.registro_service.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "biohub")
public record PropiedadesRegistro(@Valid @NotNull Eventos eventos, @Valid @NotNull Autorizacion autorizacion) {

    public record Eventos(@NotBlank String exchange, @NotNull @Positive Long intervaloMs,
                          @NotNull @Positive Integer lote) {
    }

    public record Autorizacion(@NotNull URI url, @NotNull Duration cacheTtl) {
    }
}
