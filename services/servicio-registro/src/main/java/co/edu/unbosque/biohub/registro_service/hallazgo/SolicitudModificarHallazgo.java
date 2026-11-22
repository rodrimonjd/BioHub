package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.constraints.NotBlank;

public record SolicitudModificarHallazgo(
        @NotBlank String campo,
        @NotBlank String valor,
        @NotBlank String autor) { }