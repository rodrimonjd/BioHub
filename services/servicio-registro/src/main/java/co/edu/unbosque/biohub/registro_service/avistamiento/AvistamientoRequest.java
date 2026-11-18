package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.*;

public record AvistamientoRequest(
        @NotBlank String investigadorId,
        @NotNull UUID especieId,
        @NotNull @PastOrPresent Instant fechaHora,
        @Min(1) int cantidad,
        @DecimalMin("-90") @DecimalMax("90") Double latitud,
        @DecimalMin("-180") @DecimalMax("180") Double longitud,
        @NotBlank String departamento,
        String region) { }