package co.edu.unbosque.biohub.reportes.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuración propia de servicio-reportes (prefijo {@code reportes} del application.yml).
 * Se valida al arrancar: si falta un valor o es inválido, el servicio no inicia y dice cuál.
 *
 * @param eventos                 colas de RabbitMQ con los avistamientos que alimentan los reportes
 * @param intervaloGeneracionMs   cada cuántos milisegundos se revisan los reportes en cola para generarlos
 */
@Validated
@ConfigurationProperties(prefix = "reportes")
public record PropiedadesReportes(@Valid @NotNull Eventos eventos, @NotNull @Positive Long intervaloGeneracionMs) {

    /**
     * @param exchange     exchange topic de RabbitMQ donde publica servicio-registro
     * @param cola         cola durable de la que consume el servicio de reportes
     * @param colaErrores  cola de mensajes que no se pudieron procesar (dead letter)
     */
    public record Eventos(@NotBlank String exchange, @NotBlank String cola, @NotBlank String colaErrores) {
    }
}
