package co.edu.unbosque.biohub.reportes.reporte;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudReporte(
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        String departamento,
        String especieId,
        @NotBlank String solicitadoPor) { }
