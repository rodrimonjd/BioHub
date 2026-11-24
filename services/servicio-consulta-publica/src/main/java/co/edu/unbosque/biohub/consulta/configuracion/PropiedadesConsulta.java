package co.edu.unbosque.biohub.consulta.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuración propia de servicio-consulta-publica (prefijo {@code consulta} del application.yml).
 * Se valida al arrancar: si falta un valor o es inválido, el servicio no inicia y dice cuál.
 *
 * @param eventos          colas de RabbitMQ con los hallazgos que se proyectan para la consulta pública
 * @param registroUrl      URL base de servicio-registro (catálogo de especies)
 * @param autorizacionUrl  URL base de servicio-autorizacion (nivel de visibilidad de la ubicación)
 * @param cache            tiempos de caché
 */
@Validated
@ConfigurationProperties(prefix = "consulta")
public record PropiedadesConsulta(@Valid @NotNull Eventos eventos, @NotNull URI registroUrl,
                                  @NotNull URI autorizacionUrl, @Valid @NotNull Cache cache) {

    /**
     * @param exchange     exchange topic de RabbitMQ donde publica servicio-registro
     * @param cola         cola durable de la que consume la consulta pública
     * @param colaErrores  cola de mensajes que no se pudieron procesar (dead letter)
     */
    public record Eventos(@NotBlank String exchange, @NotBlank String cola, @NotBlank String colaErrores) {
    }

    /**
     * @param especiesTtl    cuánto se guarda en caché el catálogo de especies de registro
     * @param visibilidadTtl cuánto se guarda en caché el nivel de visibilidad de un usuario
     */
    public record Cache(@NotNull Duration especiesTtl, @NotNull Duration visibilidadTtl) {
    }
}
