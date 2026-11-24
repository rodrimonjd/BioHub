package co.edu.unbosque.biohub.historial.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuración propia de servicio-historial (prefijo {@code historial} del application.yml).
 * Se valida al arrancar: si falta un valor o es inválido, el servicio no inicia y dice cuál.
 *
 * @param eventos          colas de RabbitMQ de las que se alimenta el historial
 * @param autorizacionUrl  URL base de servicio-autorizacion
 * @param permisoConsulta  permiso que exige la consulta del historial (p. ej. historial:ver)
 * @param cache            tiempos de caché
 */
@Validated
@ConfigurationProperties(prefix = "historial")
public record PropiedadesHistorial(@Valid @NotNull Eventos eventos, @NotNull URI autorizacionUrl,
                                   @NotBlank String permisoConsulta, @Valid @NotNull Cache cache) {

    /**
     * @param exchange     exchange topic de RabbitMQ donde publican registro y taxonomía
     * @param cola         cola durable de la que consume el historial
     * @param colaErrores  cola de mensajes que no se pudieron procesar (dead letter)
     */
    public record Eventos(@NotBlank String exchange, @NotBlank String cola, @NotBlank String colaErrores) {
    }

    /**
     * @param permisosTtl cuánto se guarda en caché la respuesta de un permiso
     */
    public record Cache(@NotNull Duration permisosTtl) {
    }
}
