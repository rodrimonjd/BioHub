package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.*;

/** investigadorId se toma de la cabecera X-Usuario-Id: el valor que venga en el cuerpo se ignora. */
public record SolicitudAvistamiento(
        String investigadorId,
        @NotNull UUID especieId,
        @NotNull @PastOrPresent Instant fechaHora,
        @Min(1) int cantidad,
        @DecimalMin("-90") @DecimalMax("90") Double latitud,
        @DecimalMin("-180") @DecimalMax("180") Double longitud,
        @NotBlank String departamento,
        String region) {

    public SolicitudAvistamiento conInvestigador(String usuarioId) {
        return new SolicitudAvistamiento(usuarioId, especieId, fechaHora, cantidad, latitud, longitud,
                departamento, region);
    }
}
