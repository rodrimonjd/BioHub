package co.edu.unbosque.biohub.registro_service.expedicion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudExpedicion(
        @NotBlank String nombre,
        @NotBlank String zona,
        @NotNull LocalDate fechaInicio,
        LocalDate fechaFin,
        @NotBlank String liderId) {
}