package co.edu.unbosque.biohub.registro_service.configuracion;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuración propia de servicio-registro (prefijo {@code biohub} del application.yml).
 * Se valida al arrancar: si falta un valor o es inválido, el servicio no inicia y dice cuál.
 *
 * @param eventos       publicación de eventos de dominio (outbox → RabbitMQ)
 * @param autorizacion  verificación de permisos en servicio-autorizacion
 */
@Validated
@ConfigurationProperties(prefix = "biohub")
public record PropiedadesRegistro(@Valid @NotNull Eventos eventos, @Valid @NotNull Autorizacion autorizacion) {

    /**
     * @param exchange    exchange topic de RabbitMQ donde se publican los eventos
     * @param intervaloMs cada cuántos milisegundos el outbox reintenta publicar los eventos pendientes
     * @param lote        máximo de eventos del outbox que se publican en cada pasada
     */
    public record Eventos(@NotBlank String exchange, @NotNull @Positive Long intervaloMs,
                          @NotNull @Positive Integer lote) {
    }

    /**
     * @param url      URL base de servicio-autorizacion
     * @param cacheTtl cuánto se guarda en caché la respuesta de un permiso
     */
    public record Autorizacion(@NotNull URI url, @NotNull Duration cacheTtl) {
    }
}
