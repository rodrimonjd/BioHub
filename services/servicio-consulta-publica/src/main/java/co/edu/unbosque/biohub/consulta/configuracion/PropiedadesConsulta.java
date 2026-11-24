package co.edu.unbosque.biohub.consulta.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "consulta")
public record PropiedadesConsulta(@Valid @NotNull Eventos eventos, @NotNull URI registroUrl,
                                  @NotNull URI autorizacionUrl, @Valid @NotNull Cache cache) {

    public record Eventos(@NotBlank String exchange, @NotBlank String cola, @NotBlank String colaErrores) {
    }

    public record Cache(@NotNull Duration especiesTtl, @NotNull Duration visibilidadTtl) {
    }
}
